strComputer =  
strDomain =  
strUser = 
strPassword = 
Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")

If IsEmpty (strDomain) Then
Set objSWbemServices = objSWbemLocator.ConnectServer(strComputer,"root\cimv2",strUser,strPassword,"MS_409","ntlmdomain:" + strDomain) 
Else
Set objSWbemServices = objSWbemLocator.ConnectServer(strComputer,"root\cimv2",strUser,strPassword)
End If

Set colSwbemObjectSet = objSWbemServices.ExecQuery("Select * From Win32_Process")
For Each objProcess in colSWbemObjectSet
    Wscript.Echo "Process Name: " & objProcess.Name 
Next