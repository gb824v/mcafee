package com.mcafee.mam.auto.infra.build;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import com.mcafee.mam.auto.infra.TestException;

/**
 * represents a client that can log into everest web interface.
 * 
 * @author Guy
 */
public class JenkinsClient
{
	private HttpClient httpClient = null;
	HttpGet httpGetRequest = new HttpGet();
	private String host = "";
	private String port = "8080";

	JenkinsClient(String host) throws TestException
	{
		this(host, "");
	}

	public JenkinsClient(String host, String port) throws TestException
	{
		this.host = host;
		if (!port.isEmpty()) this.port = port;
		try
		{
			this.httpClient = new DefaultHttpClient();
			HttpParams params = this.httpClient.getParams();
			HttpConnectionParams.setConnectionTimeout(params, (int) TimeUnit.MINUTES.toMillis(15));
			HttpConnectionParams.setSoTimeout(params, (int) TimeUnit.MINUTES.toMillis(15));
			params.setIntParameter("http.connection.timeout", (int) TimeUnit.MINUTES.toMillis(15));
			params.setBooleanParameter("http.protocol.expect-continue", true);
			HttpConnectionParams.setStaleCheckingEnabled(params, true);
			HttpConnectionParams.setSoReuseaddr(params, true);
			HttpConnectionParams.setTcpNoDelay(params, true);
		}
		catch (Exception ex)
		{
			throw new TestException("Failed to init http socket for jenkins", ex);
		}
	}

	private void initLocalHttpGetRequest()
	{
		if (this.httpGetRequest != null)
		{
			if (!this.httpGetRequest.isAborted())
			{
				this.httpGetRequest.abort();
				this.httpGetRequest = null;
			}
		}
	}

	/**
	 * Verify if the WebCommand response code is 200OK
	 * 
	 * @param response
	 * @throws TestException
	 */
	public void verifyResponse(HttpResponse response) throws TestException
	{
		if (response != null)
		{
			if (response.getStatusLine().getStatusCode() != 200) { throw new TestException("request failed " + response.getStatusLine().getReasonPhrase()); }
		}
	}

	public String getURL(String command)
	{
		return String.format("http://%s:%s/%s", this.host, this.port, command);
	}

	/**
	 * 
	 * @param cmd
	 * @return
	 * @throws TestException
	 * @throws Exception
	 */
	public InputStream executeHttpGet(String cmd) throws TestException, Exception
	{
		String url = getURL(cmd);
		initLocalHttpGetRequest();
		this.httpGetRequest = new HttpGet(url);
		try
		{
			HttpResponse response = this.httpClient.execute(this.httpGetRequest);
			verifyResponse(response);
			return response.getEntity().getContent();
		}
		catch (Exception e)
		{
			this.httpGetRequest.abort();
			throw new TestException(e.getMessage());
		}
	}
}
