package com.mcafee.mam.auto.infra.drivers.vsphere;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;
import org.apache.log4j.Logger;

import com.google.common.base.Joiner;
import com.mcafee.mam.auto.infra.Record;
import com.mcafee.mam.auto.infra.TestException;
import com.vmware.vim25.*;

public class VSphere2Api
{
	private static Logger logger = Logger.getLogger(VSphere2Api.class);
	/* Start Server Connection and common code */
	private final String SVC_INST_NAME = "ServiceInstance";
	private final ManagedObjectReference SVC_INST_REF = new ManagedObjectReference();
	private String tempFilePath = null;
	private ManagedObjectReference fileManagerRef = null;
	private ManagedObjectReference processManagerRef = null;
	private VirtualMachinePowerState powerState;
	private NamePasswordAuthentication auth = null;
	private VimService vimService = null;
	public VimPortType vimPort = null;
	public ServiceContent serviceContent = null;
	private ManagedObjectReference propCollector;
	private ManagedObjectReference rootFolderRef = null;
	public String datacenter = null;
	private boolean isConnected = true;
	private String url = null;
	private String user = null;
	private String password = null;
	public String host = null;
	private boolean isOverwrite = true;

	public VSphere2Api(String host, String user, String password, boolean connect) throws Exception
	{
		this.host = host;
		this.url = String.format("https://%s/sdk/", host);
		this.user = user;
		this.password = password;
		if (connect)
		{
			this.connect();
		}
	}

	/**
	 * Set the managed object reference type, and value to ServiceInstance.
	 */
	private void initSvcInstRef()
	{
		SVC_INST_REF.setType(SVC_INST_NAME);
		SVC_INST_REF.setValue(SVC_INST_NAME);
	}

	/**
	 * 
	 * @param url
	 *            The URL of the vCenter Server
	 * 
	 *            https://<Server IP / host name>/sdk
	 * 
	 *            The method establishes a connection with the web service port on the server. This
	 *            is not to be confused with the session connection.
	 * 
	 */
	private void initVimPort() throws Exception
	{
		vimService = new VimService();
		vimPort = vimService.getVimPort();
		Map<String, Object> ctxt = ((BindingProvider) vimPort).getRequestContext();
		ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
		ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
	}

	private void initServiceContent() throws Exception
	{
		if (serviceContent == null)
		{
			serviceContent = v
					mPort.retrieveServiceContent(SVC_INST_REF);
			if (serviceContent == null) { throw new Exception("Could not get Service Content"); }
		}
	}

	private void initPropertyCollector() throws Exception
	{
		if (propCollector == null)
		{
			propCollector = serviceContent.getPropertyCollector();
			if (propCollector == null) { throw new Exception("Could not get Property Collector"); }
		}
	}

	private void initRootFolder() throws Exception
	{
		if (rootFolderRef == null)
		{
			rootFolderRef = serviceContent.getRootFolder();
			if (rootFolderRef == null) { throw new Exception("Could not get Root Folder"); }
		}
	}

	/**
	 * Establishes session with the virtual center server.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void connect() throws Exception
	{
		HostnameVerifier hv = new HostnameVerifier()
		{
			@Override
			public boolean verify(String urlHostName, SSLSession session)
			{
				return true;
			}
		};
		trustAllHttpsCertificates();
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
		// These following methods have to be called in this order.
		initSvcInstRef();
		initVimPort();
		initServiceContent();
		vimPort.login(serviceContent.getSessionManager(), user, password, null);
		initPropertyCollector();
		initRootFolder();
		isConnected = true;
	}

	/**
	 * Disconnects the user session.
	 * 
	 * @throws Exception
	 */
	public void disconnect()
	{
		if (isConnected)
		{
			try
			{
				vimPort.logout(serviceContent.getSessionManager());
			}
			catch (Exception e)
			{
				logger.trace("Failed to logout esx: " + host, e);
			}
		}
		isConnected = false;
	}

	public void powerOff(String vmname) throws Exception
	{
		ManagedObjectReference vmMor = getMOREFsInContainerByType(serviceContent.getRootFolder(), "VirtualMachine").get(vmname);

		try
		{
			logger.info("Powering off virtual machine : " + vmname + "[" + vmMor.getValue() + "]");
			ManagedObjectReference taskmor = vimPort.powerOffVMTask(vmMor);
			if (getTaskResultAfterDone(taskmor))
			{
				logger.info(vmname + "[" + vmMor.getValue() + "] powered off successfully");
			}
		}
		catch (Exception e)
		{
			throw new TestException("Unable to poweroff vm : " + vmname + "[" + vmMor.getValue() + "]", e);
		}
	}

	public List<Record> getVmGuestNetInfo(String vmname) throws Exception
	{
		List<Record> recordList = new ArrayList<Record>();
		if (isPowerOn(vmname))
		{
			waitForVmConnected(vmname);
			ManagedObjectReference vmMor = getMOREFsInContainerByType(serviceContent.getRootFolder(), "VirtualMachine").get(vmname);
			ArrayOfGuestNicInfo arrOfGuestNicInfo = (ArrayOfGuestNicInfo) getEntityProps(vmMor, new String[] { "guest.net" }).get("guest.net");

			for (GuestNicInfo nicInfo : arrOfGuestNicInfo.getGuestNicInfo())
			{
				Record rec = new Record();
				rec.add("mac", nicInfo.getMacAddress());
				rec.add("network", nicInfo.getNetwork());
				if (nicInfo.getIpAddress().size() > 1)
				{
					rec.add("ip.v6", nicInfo.getIpAddress().get(0));
					rec.add("ip.v4", nicInfo.getIpAddress().get(1));
				}
				else
				{
					rec.add("ip.v4", nicInfo.getIpAddress().get(0));
				}
				if (nicInfo.getDnsConfig() != null)
				{
					rec.add("dns.domain", nicInfo.getDnsConfig().getDomainName());
					rec.add("dns.ips", Joiner.on(",").join(nicInfo.getDnsConfig().getIpAddress()));
				}
				if (nicInfo.getIpConfig() != null)
				{
					if (nicInfo.getIpConfig().getDhcp() != null)
					{
						rec.add("dhcp.enabled", Boolean.toString(nicInfo.getIpConfig().getDhcp().getIpv4().isEnable()));
					}
				}
				recordList.add(rec);
			}
		}
		else
		{
			logger.info("VmGuestNetInfo: [" + vmname + "] is powerOff");
		}
		return recordList;
	}

	private boolean isVmOk(String vmname, String... excludeList)
	{
		for (String exc : excludeList)
		{
			if (vmname.equals(exc)) { return true; }
		}
		return false;
	}

	public void powerOffAll(String... excludeList) throws Exception
	{
		Map<String, ManagedObjectReference> vms = getMOREFsInContainerByType(serviceContent.getRootFolder(), "VirtualMachine");

		for (String vmname : vms.keySet())
		{
			ManagedObjectReference vmMor = vms.get(vmname);
			try
			{
				if (!isVmOk(vmname, excludeList))
				{
					if (isPowerOn(vmname))
					{
						logger.info("Powering off virtual machine : " + vmname + "[" + vmMor.getValue() + "]");
						ManagedObjectReference taskmor = vimPort.powerOffVMTask(vmMor);
						if (getTaskResultAfterDone(taskmor))
						{
							logger.info(vmname + "[" + vmMor.getValue() + "] Powered off successfully");
						}
					}
					else

					{
						logger.info(vmname + "[" + vmMor.getValue() + "] Already powerOff");
					}
				}

			}
			catch (Exception e)
			{
				throw new TestException("Unable to poweroff vm : " + vmname + "[" + vmMor.getValue() + "]", e);
			}
		}
	}

	public void shutdownAll(String... excludeList) throws Exception
	{
		Map<String, ManagedObjectReference> vms = getMOREFsInContainerByType(serviceContent.getRootFolder(), "VirtualMachine");

		for (String vmname : vms.keySet())
		{
			ManagedObjectReference vmMor = vms.get(vmname);
			try
			{
				if (isPowerOn(vmname))
				{

					if (!isVmOk(vmname, excludeList))
					{
						logger.info("Shutting down guest os in virtual machine : " + vmname + "[" + vmMor.getValue() + "]");
						vimPort.shutdownGuest(vmMor);
						logger.info("Guest os in vm : " + vmname + "[" + vmMor.getValue() + "]" + " shutdown");
					}
					else
					{
						logger.info("Guest os in vm : " + vmname + "[" + vmMor.getValue() + "]" + " should be on");
					}
				}
				else

				{
					logger.info(vmname + "[" + vmMor.getValue() + "] Guset already down");
				}
			}
			catch (Exception e)
			{
				throw new TestException("Unable to down guest vm : " + vmname + "[" + vmMor.getValue() + "]", e);
			}
		}
	}

	public void updateExsistingVmNic(String vmName, String mac, String networkName) throws Exception
	{
		ManagedObjectReference vmMor = getMOREFsInContainerByType(serviceContent.getRootFolder(), "VirtualMachine").get(vmName);
		try
		{
			VirtualMachineConfigSpec machineConfigSpec = new VirtualMachineConfigSpec();
			VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
			VirtualEthernetCard nic = null;
			nicSpec.setOperation(VirtualDeviceConfigSpecOperation.EDIT);
			List<VirtualDevice> listvd = ((ArrayOfVirtualDevice) getEntityProps(vmMor, new String[] { "config.hardware.device" }).get("config.hardware.device")).getVirtualDevice();
			for (VirtualDevice device : listvd)
			{
				if (device instanceof VirtualEthernetCard)
				{
					nic = (VirtualEthernetCard) device;
					if (nic.getMacAddress().equals(mac)) break;
				}
			}
			if (nic != null)
			{
				VirtualEthernetCardNetworkBackingInfo nicBacking = new VirtualEthernetCardNetworkBackingInfo();
				nicBacking.setDeviceName(networkName);
				nic.setBacking(nicBacking);
				nicSpec.setDevice(nic);
			}
			List<VirtualDeviceConfigSpec> deviceConfigSpec = new ArrayList<VirtualDeviceConfigSpec>();
			deviceConfigSpec.add(nicSpec);
			machineConfigSpec.getDeviceChange().addAll(deviceConfigSpec);
			ManagedObjectReference taskmor = vimPort.reconfigVMTask(vmMor, machineConfigSpec);

			if (getTaskResultAfterDone(taskmor))
			{
				logger.info(vmName + "[" + vmMor.getValue() + "] update Vm Nic successfully");
			}
		}
		catch (Exception e)
		{
			throw new TestException("Unable to update Vm Net Connection vm : " + vmName, e);
		}
	}

	public void addVmNic(String vmName, String networkName) throws Exception
	{
		ManagedObjectReference vmMor = getMOREFsInContainerByType(serviceContent.getRootFolder(), "VirtualMachine").get(vmName);
		try
		{
			VirtualMachineConfigSpec machineConfigSpec = new VirtualMachineConfigSpec();
			// Add a NIC. the network Name must be set as the device name to create the NIC.
			VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
			if (networkName != null)
			{
				nicSpec.setOperation(VirtualDeviceConfigSpecOperation.ADD);
				VirtualE1000 nic = new VirtualE1000();
				VirtualEthernetCardNetworkBackingInfo nicBacking = new VirtualEthernetCardNetworkBackingInfo();
				nicBacking.setDeviceName(networkName);
				nicBacking.setUseAutoDetect(true);
				nic.setAddressType("generated");
				nic.setBacking(nicBacking);
				nic.setKey(4);
				nicSpec.setDevice(nic);

			}

			List<VirtualDeviceConfigSpec> deviceConfigSpec = new ArrayList<VirtualDeviceConfigSpec>();
			deviceConfigSpec.add(nicSpec);
			machineConfigSpec.getDeviceChange().addAll(deviceConfigSpec);

			ManagedObjectReference taskmor = vimPort.reconfigVMTask(vmMor, machineConfigSpec);

			if (getTaskResultAfterDone(taskmor))
			{
				logger.info(vmName + "[" + vmMor.getValue() + "] add Vm Nic created successfully");
			}
		}
		catch (Exception e)
		{
			throw new TestException("Unable to add Vm nic Connection vm : " + vmName, e);
		}
	}

	public void removeAllVmNics(String vmName) throws Exception
	{
		ManagedObjectReference vmMor = getMOREFsInContainerByType(serviceContent.getRootFolder(), "VirtualMachine").get(vmName);
		try
		{
			VirtualMachineConfigSpec machineConfigSpec = new VirtualMachineConfigSpec();
			VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
			VirtualEthernetCard nic = null;
			nicSpec.setOperation(VirtualDeviceConfigSpecOperation.REMOVE);
			List<VirtualDevice> listvd = ((ArrayOfVirtualDevice) getEntityProps(vmMor, new String[] { "config.hardware.device" }).get("config.hardware.device")).getVirtualDevice();
			for (VirtualDevice device : listvd)
			{
				if (device instanceof VirtualEthernetCard)
				{
					nic = (VirtualEthernetCard) device;
					break;
				}
			}
			if (nic != null)
			{
				nicSpec.setDevice(nic);
			}
			List<VirtualDeviceConfigSpec> deviceConfigSpec = new ArrayList<VirtualDeviceConfigSpec>();
			deviceConfigSpec.add(nicSpec);
			machineConfigSpec.getDeviceChange().addAll(deviceConfigSpec);
			ManagedObjectReference taskmor = vimPort.reconfigVMTask(vmMor, machineConfigSpec);

			if (getTaskResultAfterDone(taskmor))
			{
				logger.info(vmName + "[" + vmMor.getValue() + "] remove Vm Nic successfully");
			}
		}
		catch (Exception e)
		{
			throw new TestException("Unable to remove Vm Net Connection vm : " + vmName, e);
		}
	}

	protected boolean isPowerOn(String vmname) throws Exception
	{
		ManagedObjectReference vmMor = getMOREFsInContainerByType(serviceContent.getRootFolder(), "VirtualMachine").get(vmname);
		VirtualMachinePowerState powerState = (VirtualMachinePowerState) getEntityProps(vmMor, new String[] { "runtime.powerState" }).get("runtime.powerState");
		return (powerState.equals(VirtualMachinePowerState.POWERED_ON));
	}

	protected void waitForVmConnected(String vmname) throws Exception
	{
		ManagedObjectReference vmMor = getMOREFsInContainerByType(serviceContent.getRootFolder(), "VirtualMachine").get(vmname);
		if (vmMor != null)
		{
			waitForValues(vmMor, new String[] { "guest.guestOperationsReady" }, new String[] { "guest.guestOperationsReady" }, new Object[][] { new Object[] { true } });
		}
	}

	public void powerOn(String vmname) throws Exception
	{
		ManagedObjectReference vmMor = getMOREFsInContainerByType(serviceContent.getRootFolder(), "VirtualMachine").get(vmname);
		try
		{
			logger.info("Powering on virtual machine : " + vmname + "[" + vmMor.getValue() + "]");
			ManagedObjectReference taskmor = vimPort.powerOnVMTask(vmMor, null);
			if (getTaskResultAfterDone(taskmor))
			{
				logger.info(vmname + "[" + vmMor.getValue() + "] powered on successfully");
			}
		}
		catch (Exception e)
		{
			throw new TestException("Unable to poweron vm : " + vmname + "[" + vmMor.getValue() + "]", e);
		}
	}

	public void reset(ArrayList<String> hostnames) throws Exception
	{
		for (String hostName : hostnames)
		{
			reset(hostName);
		}
	}

	public void reset(String vmname) throws Exception
	{
		ManagedObjectReference vmMor = getMOREFsInContainerByType(serviceContent.getRootFolder(), "VirtualMachine").get(vmname);
		try
		{
			logger.info("Reseting virtual machine : " + vmname + "[" + vmMor.getValue() + "]");
			ManagedObjectReference taskmor = vimPort.resetVMTask(vmMor);
			if (getTaskResultAfterDone(taskmor))
			{
				logger.info(vmname + "[" + vmMor.getValue() + "] reset successfully");
			}
		}
		catch (Exception e)
		{
			throw new TestException("Unable to reset vm : " + vmname + "[" + vmMor.getValue() + "]", e);
		}
	}

	public void suspend(String vmname) throws Exception
	{
		ManagedObjectReference vmMor = getMOREFsInContainerByType(serviceContent.getRootFolder(), "VirtualMachine").get(vmname);
		try
		{
			logger.info("Suspending virtual machine : " + vmname + "[" + vmMor.getValue() + "]");
			ManagedObjectReference taskmor = vimPort.suspendVMTask(vmMor);
			if (getTaskResultAfterDone(taskmor))
			{
				logger.info(vmname + "[" + vmMor.getValue() + "] suspended successfully");
			}
		}
		catch (Exception e)
		{
			throw new TestException("Unable to suspend vm : " + vmname + "[" + vmMor.getValue() + "]", e);
		}
	}

	public void reboot(String vmname) throws Exception
	{
		ManagedObjectReference vmMor = getMOREFsInContainerByType(serviceContent.getRootFolder(), "VirtualMachine").get(vmname);
		try
		{
			logger.info("Rebooting guest os in virtual machine : " + vmname + "[" + vmMor.getValue() + "]");
			vimPort.rebootGuest(vmMor);
			logger.info("Guest os in vm : " + vmname + "[" + vmMor.getValue() + "]" + " rebooted");
		}
		catch (Exception e)
		{
			throw new TestException("Unable to reboot guest os in vm : " + vmname + "[" + vmMor.getValue() + "]", e);
		}
	}

	public void shutdown(String vmname) throws Exception
	{
		ManagedObjectReference vmMor = getMOREFsInContainerByType(serviceContent.getRootFolder(), "VirtualMachine").get(vmname);
		try
		{
			logger.info("Shutting down guest os in virtual machine : " + vmname + "[" + vmMor.getValue() + "]");
			vimPort.shutdownGuest(vmMor);
			logger.info("Guest os in vm : " + vmname + "[" + vmMor.getValue() + "]" + " shutdown");
		}
		catch (Exception e)
		{
			logger.info("Unable to shutdown guest os in vm : " + vmname + "[" + vmMor.getValue() + "]");
			System.err.println("Reason :" + e.getLocalizedMessage());
		}
	}

	public void standby(String vmname) throws Exception
	{
		ManagedObjectReference vmMor = getMOREFsInContainerByType(serviceContent.getRootFolder(), "VirtualMachine").get(vmname);
		try
		{
			logger.info("Putting the guest os in virtual machine : " + vmname + "[" + vmMor.getValue() + "] in standby mode");
			vimPort.standbyGuest(vmMor);
			logger.info("Guest os in vm : " + vmname + "[" + vmMor.getValue() + "]" + " in standby mode");
		}
		catch (Exception e)
		{
			throw new TestException("Unable to put the guest os in vm : " + vmname + "[" + vmMor.getValue() + "] to standby mode", e);
		}
	}

	/**
	 * Method to retrieve properties of a {@link ManagedObjectReference}
	 * 
	 * @param entityMor
	 *            {@link ManagedObjectReference} of the entity
	 * @param props
	 *            Array of properties to be looked up
	 * @return Map of the property name and its corresponding value
	 * 
	 * @throws InvalidPropertyFaultMsg
	 *             If a property does not exist
	 * @throws RuntimeFaultFaultMsg
	 */
	private Map<String, Object> getEntityProps(ManagedObjectReference entityMor, String[] props) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg
	{

		HashMap<String, Object> retVal = new HashMap<String, Object>();

		// Create Property Spec
		PropertySpec propertySpec = new PropertySpec();
		propertySpec.setAll(Boolean.FALSE);
		propertySpec.setType(entityMor.getType());
		propertySpec.getPathSet().addAll(Arrays.asList(props));

		// Now create Object Spec
		ObjectSpec objectSpec = new ObjectSpec();
		objectSpec.setObj(entityMor);

		// Create PropertyFilterSpec using the PropertySpec and ObjectPec
		// created above.
		PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
		propertyFilterSpec.getPropSet().add(propertySpec);
		propertyFilterSpec.getObjectSet().add(objectSpec);

		List<PropertyFilterSpec> propertyFilterSpecs = new ArrayList<PropertyFilterSpec>();
		propertyFilterSpecs.add(propertyFilterSpec);
		List<ObjectContent> oCont = vimPort.retrieveProperties(propCollector, propertyFilterSpecs);
		if (oCont != null)
		{
			for (ObjectContent oc : oCont)
			{
				List<DynamicProperty> dps = oc.getPropSet();
				if (dps != null)
				{
					for (DynamicProperty dp : dps)
					{
						retVal.put(dp.getName(), dp.getVal());
					}
				}
			}
		}
		return retVal;
	}

	/**
	 * Returns all the MOREFs of the specified type that are present under the container
	 * 
	 * @param folder
	 *            {@link ManagedObjectReference} of the container to begin the search from
	 * @param morefType
	 *            Type of the managed entity that needs to be searched
	 * 
	 * @return Map of name and MOREF of the managed objects present. If none exist then empty Map is
	 *         returned
	 * 
	 * @throws InvalidPropertyFaultMsg
	 * @throws RuntimeFaultFaultMsg
	 */
	private Map<String, ManagedObjectReference> getMOREFsInContainerByType(ManagedObjectReference folder, String morefType) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg
	{
		String PROP_ME_NAME = "name";
		ManagedObjectReference viewManager = serviceContent.getViewManager();
		ManagedObjectReference containerView = vimPort.createContainerView(viewManager, folder, Arrays.asList(morefType), true);

		Map<String, ManagedObjectReference> tgtMoref = new HashMap<String, ManagedObjectReference>();

		// Create Property Spec
		PropertySpec propertySpec = new PropertySpec();
		propertySpec.setAll(Boolean.FALSE);
		propertySpec.setType(morefType);
		propertySpec.getPathSet().add(PROP_ME_NAME);

		TraversalSpec ts = new TraversalSpec();
		ts.setName("view");
		ts.setPath("view");
		ts.setSkip(false);
		ts.setType("ContainerView");

		// Now create Object Spec
		ObjectSpec objectSpec = new ObjectSpec();
		objectSpec.setObj(containerView);
		objectSpec.setSkip(Boolean.TRUE);
		objectSpec.getSelectSet().add(ts);

		// Create PropertyFilterSpec using the PropertySpec and ObjectPec
		// created above.
		PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
		propertyFilterSpec.getPropSet().add(propertySpec);
		propertyFilterSpec.getObjectSet().add(objectSpec);

		List<PropertyFilterSpec> propertyFilterSpecs = new ArrayList<PropertyFilterSpec>();
		propertyFilterSpecs.add(propertyFilterSpec);

		RetrieveResult rslts = vimPort.retrievePropertiesEx(propCollector, propertyFilterSpecs, new RetrieveOptions());
		List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();
		if (rslts != null && rslts.getObjects() != null && !rslts.getObjects().isEmpty())
		{
			listobjcontent.addAll(rslts.getObjects());
		}
		String token = null;
		if (rslts != null && rslts.getToken() != null)
		{
			token = rslts.getToken();
		}
		while (token != null && !token.isEmpty())
		{
			rslts = vimPort.continueRetrievePropertiesEx(propCollector, token);
			token = null;
			if (rslts != null)
			{
				token = rslts.getToken();
				if (rslts.getObjects() != null && !rslts.getObjects().isEmpty())
				{
					listobjcontent.addAll(rslts.getObjects());
				}
			}
		}
		for (ObjectContent oc : listobjcontent)
		{
			ManagedObjectReference mr = oc.getObj();
			String entityNm = null;
			List<DynamicProperty> dps = oc.getPropSet();
			if (dps != null)
			{
				for (DynamicProperty dp : dps)
				{
					entityNm = (String) dp.getVal();
				}
			}
			tgtMoref.put(entityNm, mr);
		}
		return tgtMoref;
	}

	private boolean getTaskResultAfterDone(ManagedObjectReference task) throws Exception
	{

		boolean retVal = false;

		// info has a property - state for state of the task
		Object[] result = waitForValues(task, new String[] { "info.state", "info.error" }, new String[] { "state" }, new Object[][] { new Object[] { TaskInfoState.SUCCESS,
				TaskInfoState.ERROR } });

		if (result[0].equals(TaskInfoState.SUCCESS))
		{
			retVal = true;
		}
		if (result[1] instanceof LocalizedMethodFault) { throw new RuntimeException(((LocalizedMethodFault) result[1]).getLocalizedMessage()); }
		return retVal;
	}

	private Object[] waitForValues(ManagedObjectReference objmor, String[] filterProps, String[] endWaitProps, Object[][] expectedVals) throws Exception
	{
		// version string is initially null
		String version = "";
		Object[] endVals = new Object[endWaitProps.length];
		Object[] filterVals = new Object[filterProps.length];

		PropertyFilterSpec spec = new PropertyFilterSpec();
		ObjectSpec oSpec = new ObjectSpec();
		oSpec.setObj(objmor);
		oSpec.setSkip(Boolean.FALSE);
		spec.getObjectSet().add(oSpec);

		PropertySpec pSpec = new PropertySpec();
		pSpec.getPathSet().addAll(Arrays.asList(filterProps));
		pSpec.setType(objmor.getType());
		spec.getPropSet().add(pSpec);

		ManagedObjectReference filterSpecRef = vimPort.createFilter(propCollector, spec, true);

		boolean reached = false;

		UpdateSet updateset = null;
		List<PropertyFilterUpdate> filtupary = null;
		List<ObjectUpdate> objupary = null;
		List<PropertyChange> propchgary = null;
		while (!reached)
		{
			updateset = vimPort.waitForUpdates(propCollector, version);
			if (updateset == null || updateset.getFilterSet() == null)
			{
				continue;
			}
			version = updateset.getVersion();

			// Make this code more general purpose when PropCol changes later.
			filtupary = updateset.getFilterSet();

			for (PropertyFilterUpdate filtup : filtupary)
			{
				objupary = filtup.getObjectSet();
				for (ObjectUpdate objup : objupary)
				{
					if (objup.getKind() == ObjectUpdateKind.MODIFY || objup.getKind() == ObjectUpdateKind.ENTER || objup.getKind() == ObjectUpdateKind.LEAVE)
					{
						propchgary = objup.getChangeSet();
						for (PropertyChange propchg : propchgary)
						{
							updateValues(endWaitProps, endVals, propchg);
							updateValues(filterProps, filterVals, propchg);
						}
					}
				}
			}

			Object expctdval = null;
			// Check if the expected values have been reached and exit the loop
			// if done.
			// Also exit the WaitForUpdates loop if this is the case.
			for (int chgi = 0; chgi < endVals.length && !reached; chgi++)
			{
				for (int vali = 0; vali < expectedVals[chgi].length && !reached; vali++)
				{
					expctdval = expectedVals[chgi][vali];

					reached = expctdval.equals(endVals[chgi]) || reached;
				}
			}
		}

		// Destroy the filter when we are done.
		vimPort.destroyPropertyFilter(filterSpecRef);
		return filterVals;
	}

	private void updateValues(String[] props, Object[] vals, PropertyChange propchg)
	{
		for (int findi = 0; findi < props.length; findi++)
		{
			if (propchg.getName().lastIndexOf(props[findi]) >= 0)
			{
				if (propchg.getOp() == PropertyChangeOp.REMOVE)
				{
					vals[findi] = "";
				}
				else
				{
					vals[findi] = propchg.getVal();
				}
			}
		}
	}

	/**
	 * Returns all the MOREFs of the specified type that are present under the folder
	 * 
	 * @param folder
	 *            {@link ManagedObjectReference} of the folder to begin the search from
	 * @param morefType
	 *            Type of the managed entity that needs to be searched
	 * 
	 * @return Map of name and MOREF of the managed objects present. If none exist then empty Map is
	 *         returned
	 * 
	 * @throws InvalidPropertyFaultMsg
	 * @throws RuntimeFaultFaultMsg
	 */
	private Map<String, ManagedObjectReference> getMOREFsInFolderByType(ManagedObjectReference folder, String morefType) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg
	{
		String PROP_ME_NAME = "name";
		ManagedObjectReference viewManager = serviceContent.getViewManager();
		ManagedObjectReference containerView = vimPort.createContainerView(viewManager, folder, Arrays.asList(morefType), true);

		Map<String, ManagedObjectReference> tgtMoref = new HashMap<String, ManagedObjectReference>();

		// Create Property Spec
		PropertySpec propertySpec = new PropertySpec();
		propertySpec.setAll(Boolean.FALSE);
		propertySpec.setType(morefType);

		propertySpec.getPathSet().add(PROP_ME_NAME);

		TraversalSpec ts = new TraversalSpec();
		ts.setName("view");
		ts.setPath("view");
		ts.setSkip(false);
		ts.setType("ContainerView");

		// Now create Object Spec
		ObjectSpec objectSpec = new ObjectSpec();
		objectSpec.setObj(containerView);
		objectSpec.setSkip(Boolean.TRUE);
		objectSpec.getSelectSet().add(ts);

		// Create PropertyFilterSpec using the PropertySpec and ObjectPec
		// created above.
		PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
		propertyFilterSpec.getPropSet().add(propertySpec);
		propertyFilterSpec.getObjectSet().add(objectSpec);

		List<PropertyFilterSpec> propertyFilterSpecs = new ArrayList<PropertyFilterSpec>();
		propertyFilterSpecs.add(propertyFilterSpec);

		List<ObjectContent> oCont = vimPort.retrieveProperties(propCollector, propertyFilterSpecs);
		if (oCont != null)
		{
			for (ObjectContent oc : oCont)
			{
				ManagedObjectReference mr = oc.getObj();
				String entityNm = null;
				List<DynamicProperty> dps = oc.getPropSet();
				if (dps != null)
				{
					for (DynamicProperty dp : dps)
					{
						entityNm = (String) dp.getVal();
					}
				}
				tgtMoref.put(entityNm, mr);
			}
		}
		return tgtMoref;
	}

	private static void trustAllHttpsCertificates() throws Exception
	{

		javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
		javax.net.ssl.TrustManager tm = new TrustAllTrustManager();
		trustAllCerts[0] = tm;
		javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
		javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();
		sslsc.setSessionTimeout(0);
		sc.init(null, trustAllCerts, null);
		javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}

	public static class TrustAllTrustManager implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager
	{

		@Override
		public java.security.cert.X509Certificate[] getAcceptedIssuers()
		{
			return null;
		}

		@Override
		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) throws java.security.cert.CertificateException
		{

			return;
		}

		@Override
		public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) throws java.security.cert.CertificateException
		{
			return;
		}
	}

	public boolean isSnapshotExsist(String virtualMachineName, String snapshotname) throws Exception
	{
		ManagedObjectReference vmRef = getMOREFsInContainerByType(serviceContent.getRootFolder(), "VirtualMachine").get(virtualMachineName);
		ManagedObjectReference snapmor = getSnapshotReference(vmRef, virtualMachineName, snapshotname);
		if (snapmor != null) { return true; }
		return false;
	}

	/**
	 * 
	 * @param vmname
	 * @param snapshotname
	 * @return
	 * @throws Exception
	 */
	public void revertSnapshot(String vmname, String snapshotname, boolean isForce) throws Exception
	{
		ManagedObjectReference vmRef = getMOREFsInContainerByType(serviceContent.getRootFolder(), "VirtualMachine").get(vmname);
		boolean isConfigured = false;

		if (!isForce)
		{
			isConfigured = isSnapshotAlreadyConfigured(vmRef, vmname, snapshotname);
		}

		if (!isPowerOn(vmname))
		{
			powerOn(vmname);
		}

		if (!isConfigured)
		{
			ManagedObjectReference snapmor = getSnapshotReference(vmRef, vmname, snapshotname);
			if (snapmor != null)
			{
				ManagedObjectReference taskMor = vimPort.revertToSnapshotTask(snapmor, null, false);
				if (getTaskResultAfterDone(taskMor))
				{
					System.out.printf(" Reverting Snapshot - [ %s ] Successful %n", snapshotname);
				}
			}
			else
			{
				throw new TestException(String.format(" Reverting Snapshot - [ %s ] Failure %n", snapshotname));
			}
		}

	}

	public boolean isSnapshotAlreadyConfigured(ManagedObjectReference vmmor, String vmName, String snapName) throws Exception
	{
		VirtualMachineSnapshotInfo snapInfo = (VirtualMachineSnapshotInfo) getEntityProps(vmmor, new String[] { "snapshot" }).get("snapshot");
		ManagedObjectReference snapmor = null;
		if (snapInfo != null)
		{
			List<VirtualMachineSnapshotTree> listvmst = snapInfo.getRootSnapshotList();
			snapmor = traverseSnapshotInTree(listvmst, snapName);

			if (snapmor != null && snapmor.getValue().equals(snapInfo.getCurrentSnapshot().getValue()))
			{
				logger.info("Snapshot named : " + snapName + " Already Configured On VirtualMachine : " + vmName);
				return true;
			}
		}
		return false;
	}

	private ManagedObjectReference getSnapshotReference(ManagedObjectReference vmmor, String vmName, String snapName) throws Exception
	{
		VirtualMachineSnapshotInfo snapInfo = (VirtualMachineSnapshotInfo) getEntityProps(vmmor, new String[] { "snapshot" }).get("snapshot");
		ManagedObjectReference snapmor = null;
		if (snapInfo != null)
		{
			List<VirtualMachineSnapshotTree> listvmst = snapInfo.getRootSnapshotList();
			snapmor = traverseSnapshotInTree(listvmst, snapName);

			if (snapmor == null)
			{
				logger.error("No Snapshot named : " + snapName + " found for VirtualMachine : " + vmName);
			}
		}
		else
		{
			logger.info("No Snapshots found for VirtualMachine : " + vmName);
		}
		return snapmor;
	}

	private ManagedObjectReference traverseSnapshotInTree(List<VirtualMachineSnapshotTree> snapTree, String findName)
	{
		ManagedObjectReference snapmor = null;
		if (snapTree == null) { return snapmor; }
		for (VirtualMachineSnapshotTree node : snapTree)
		{
			if (findName != null && node.getName().equalsIgnoreCase(findName))
			{
				return node.getSnapshot();
			}
			else
			{
				List<VirtualMachineSnapshotTree> listvmst = node.getChildSnapshotList();
				List<VirtualMachineSnapshotTree> childTree = listvmst;
				snapmor = traverseSnapshotInTree(childTree, findName);
			}
		}
		return snapmor;
	}

	public void runProgram(String vmName, String guestUserName, String guestPassword, String guestProgramPath) throws TestException
	{
		ManagedObjectReference vmMOR = null;
		try
		{
			Map<String, ManagedObjectReference> vms = getMOREFsInFolderByType(serviceContent.getRootFolder(), "VirtualMachine");
			vmMOR = vms.get(vmName);
			if (vmMOR != null)
			{
				logger.info("Virtual Machine " + vmName + " found");
				powerState = (VirtualMachinePowerState) getEntityProps(vmMOR, new String[] { "runtime.powerState" }).get("runtime.powerState");
				if (!powerState.equals(VirtualMachinePowerState.POWERED_ON))
				{
					logger.info("VirtualMachine: " + vmName + " needs to be powered on");
					return;
				}
			}
			else
			{
				logger.info("Virtual Machine " + vmName + " not found.");
				return;
			}

			String[] opts = new String[] { "guest.guestOperationsReady" };
			String[] opt = new String[] { "guest.guestOperationsReady" };

			waitForValues(vmMOR, opts, opt, new Object[][] { new Object[] { true } });

			logger.info("Guest Operations are ready for the VM");
			ManagedObjectReference guestOpManger = serviceContent.getGuestOperationsManager();
			Map<String, Object> guestOpMgr = getEntityProps(guestOpManger, new String[] { "processManager", "fileManager" });
			fileManagerRef = (ManagedObjectReference) guestOpMgr.get("fileManager");
			processManagerRef = (ManagedObjectReference) guestOpMgr.get("processManager");
			auth = new NamePasswordAuthentication();
			auth.setUsername(guestUserName);
			auth.setPassword(guestPassword);
			auth.setInteractiveSession(false);
			logger.info("Executing CreateTemporaryFile guest operation");
			tempFilePath = vimPort.createTemporaryFileInGuest(fileManagerRef, vmMOR, auth, "", "", "");
			logger.info("Successfully created a temporary file at: " + tempFilePath + " inside the guest");

			GuestProgramSpec spec = new GuestProgramSpec();
			spec.setProgramPath(guestProgramPath);
			spec.setArguments("> " + tempFilePath + " 2>&1");
			logger.info("Starting the specified program inside the guest");
			long pid = vimPort.startProgramInGuest(processManagerRef, vmMOR, auth, spec);
			logger.info("Process ID of the program started is: " + pid + "");

			List<Long> pidsList = new ArrayList<Long>();
			pidsList.add(pid);
			List<GuestProcessInfo> procInfo = null;
			do
			{
				logger.info("Waiting for the process to finish running.");
				procInfo = vimPort.listProcessesInGuest(processManagerRef, vmMOR, auth, pidsList);
				Thread.sleep(5 * 1000);
			}
			while (procInfo.get(0).getEndTime() == null);
			logger.info("Exit code of the program is " + procInfo.get(0).getExitCode());

		}
		catch (SOAPFaultException sfe)
		{
			printSoapFaultException(sfe);
		}
		catch (Exception ex)
		{
			logger.info(ex.getMessage());
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				if (isConnected)
				{
					if (tempFilePath != null)
					{
						vimPort.deleteFileInGuest(fileManagerRef, vmMOR, auth, tempFilePath);
					}
				}
			}
			catch (SOAPFaultException sfe)
			{
				printSoapFaultException(sfe);
			}
			catch (Exception e)
			{
				throw new TestException(e.getMessage(), e);
			}
		}
	}

	public void login(String vmName, String guestUserName, String guestPassword) throws TestException
	{
		ManagedObjectReference vmMOR = null;
		try
		{
			Map<String, ManagedObjectReference> vms = getMOREFsInFolderByType(serviceContent.getRootFolder(), "VirtualMachine");
			vmMOR = vms.get(vmName);
			if (vmMOR != null)
			{
				logger.debug("Virtual Machine " + vmName + " found");
				powerState = (VirtualMachinePowerState) getEntityProps(vmMOR, new String[] { "runtime.powerState" }).get("runtime.powerState");
				if (!powerState.equals(VirtualMachinePowerState.POWERED_ON))
				{
					logger.info("VirtualMachine: " + vmName + " needs to be powered on");
					return;
				}
			}
			else
			{
				logger.info("Virtual Machine " + vmName + " not found.");
				return;
			}

			String[] opts = new String[] { "guest.guestOperationsReady" };
			String[] opt = new String[] { "guest.guestOperationsReady" };

			waitForValues(vmMOR, opts, opt, new Object[][] { new Object[] { true } });

			logger.debug("Guest Operations are ready for the VM");
			ManagedObjectReference guestOpManger = serviceContent.getGuestOperationsManager();
			Map<String, Object> guestOpMgr = getEntityProps(guestOpManger, new String[] { "processManager", "fileManager" });
			fileManagerRef = (ManagedObjectReference) guestOpMgr.get("fileManager");
			processManagerRef = (ManagedObjectReference) guestOpMgr.get("processManager");
			auth = new NamePasswordAuthentication();
			auth.setUsername(guestUserName);
			auth.setPassword(guestPassword);
			auth.setInteractiveSession(false);
			logger.debug("Executing CreateTemporaryFile guest operation");
			tempFilePath = vimPort.createTemporaryFileInGuest(fileManagerRef, vmMOR, auth, "", "", "");
			logger.info("Successfully login to guest " + vmName);
		}
		catch (SOAPFaultException sfe)
		{
			printSoapFaultException(sfe);
		}
		catch (Exception ex)
		{
			logger.debug(ex.getMessage());
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				if (isConnected)
				{
					if (tempFilePath != null)
					{
						vimPort.deleteFileInGuest(fileManagerRef, vmMOR, auth, tempFilePath);
					}
				}
			}
			catch (SOAPFaultException sfe)
			{
				printSoapFaultException(sfe);
			}
			catch (Exception e)
			{
				throw new TestException(e.getMessage(), e);
			}
		}
	}

	public void copyFile(String vmName, String guestUserName, String guestPassword, String localFilePath, String guestFilePath) throws TestException
	{
		try
		{
			Map<String, ManagedObjectReference> vms = getMOREFsInFolderByType(serviceContent.getRootFolder(), "VirtualMachine");
			ManagedObjectReference vmMOR = vms.get(vmName);
			if (vmMOR != null)
			{
				logger.info("Virtual Machine " + vmName + " found");
				powerState = (VirtualMachinePowerState) getEntityProps(vmMOR, new String[] { "runtime.powerState" }).get("runtime.powerState");
				if (!powerState.equals(VirtualMachinePowerState.POWERED_ON))
				{
					logger.info("VirtualMachine: " + vmName + " needs to be powered on");
					return;
				}
			}
			else
			{
				logger.info("Virtual Machine " + vmName + " not found.");
				return;
			}
			String[] opts = new String[] { "guest.guestOperationsReady" };
			String[] opt = new String[] { "guest.guestOperationsReady" };
			waitForValues(vmMOR, opts, opt, new Object[][] { new Object[] { true } });

			ManagedObjectReference guestOpManger = serviceContent.getGuestOperationsManager();
			ManagedObjectReference fileManagerRef = (ManagedObjectReference) getEntityProps(guestOpManger, new String[] { "fileManager" }).get("fileManager");
			NamePasswordAuthentication auth = new NamePasswordAuthentication();
			auth.setUsername(guestUserName);
			auth.setPassword(guestPassword);
			auth.setInteractiveSession(false);
			GuestFileAttributes guestFileAttributes = null;
			guestFileAttributes = new GuestWindowsFileAttributes();
			guestFileAttributes.setAccessTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
			guestFileAttributes.setModificationTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
			logger.info("Executing UploadGuestFile guest operation");

			long fileSize = getFileSize(localFilePath);
			logger.info("Executing UploadFile guest operation");
			String fileUploadUrl = vimPort.initiateFileTransferToGuest(fileManagerRef, vmMOR, auth, guestFilePath, guestFileAttributes, fileSize, isOverwrite);
			URL tempUrlObject = new URL(url);
			fileUploadUrl = fileUploadUrl.replaceAll("\\*", tempUrlObject.getHost());
			logger.info("Uploading the file to :" + fileUploadUrl + "");
			uploadData(fileUploadUrl, localFilePath, fileSize);
			logger.info("Successfully uploaded the file");
		}
		catch (SOAPFaultException sfe)
		{
			printSoapFaultException(sfe);
		}
		catch (Exception ex)
		{
			throw new TestException(ex.getMessage(), ex);
		}
	}

	public long getFileSize(String localFilePath) throws Exception
	{
		File file = new File(localFilePath);
		if (!file.exists())
		{
			logger.info("Error finding the file: " + localFilePath);
			throw new Exception("Error finding the file: " + localFilePath);
		}

		if (file.isDirectory())
		{
			logger.info("Local file path points to a directory");
			throw new Exception("Local file path points to a directory");
		}
		logger.info("Size of the file is :" + file.length() + " bytes.");
		return file.length();
	}

	private void uploadData(String urlString, String fileName, long fileSize) throws Exception
	{
		HttpURLConnection conn = null;
		URL urlSt = new URL(urlString);
		conn = (HttpURLConnection) urlSt.openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);

		conn.setRequestProperty("Content-Type", "application/octet-stream");
		conn.setRequestMethod("PUT");
		conn.setRequestProperty("Content-Length", Long.toString(fileSize));
		OutputStream out = conn.getOutputStream();
		InputStream in = new FileInputStream(fileName);
		byte[] buf = new byte[102400];
		int len = 0;
		while ((len = in.read(buf)) > 0)
		{
			out.write(buf, 0, len);
		}
		in.close();
		out.close();

		int returnErrorCode = conn.getResponseCode();
		conn.disconnect();
		if (HttpsURLConnection.HTTP_OK != returnErrorCode) { throw new Exception("File Upload is unsuccessful"); }
	}

	private void printSoapFaultException(SOAPFaultException sfe) throws TestException
	{
		logger.info("SOAP Fault -");
		if (sfe.getFault().hasDetail())
		{
			String msg = sfe.getFault().getDetail().getFirstChild().getLocalName();
			logger.info(msg);
			throw new TestException(msg);
		}
		if (sfe.getFault().getFaultString() != null)
		{
			String msg = "\n Message: " + sfe.getFault().getFaultString();
			logger.info(msg);
			throw new TestException(msg);
		}
	}

}
