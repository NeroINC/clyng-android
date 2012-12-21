package com.clyng.mobile;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentLinkedQueue;

class RestClient {

	private static final int METHOD_GET = 1;
	private static final int METHOD_POST = 2;
	private static final int METHOD_DELETE = 3;
	private static final int METHOD_PUT = 4;

    private int timeout = 0;


	public RestClient() {
	}

    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

	public String get(String url) throws IOException {
		return doSession(url, null, METHOD_GET);
	}

	public String post(String url, String data) throws IOException {
		return doSession(url, data, METHOD_POST);
	}

    public String put(String url, String data) throws IOException {
        return doSession(url, data, METHOD_PUT);
    }

    public String delete(String url, String data) throws IOException {
        return doSession(url, null, METHOD_DELETE);
    }

	private String doSession(String url, String data, int method) throws IOException {

		HttpClient hcon = null;
		DataInputStream dis = null;


		try {
			hcon = new DefaultHttpClient();
            final HttpParams httpParameters = hcon.getParams();

            HttpConnectionParams.setSoTimeout(httpParameters, timeout);
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeout);

			HttpUriRequest request = getRequest(method, url, data);

			request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
			HttpResponse response = hcon.execute(request);

            int code = response.getStatusLine().getStatusCode();
            if (code != HttpStatus.SC_OK) {
                throw new IOException("Http response code is: " + code);
            }

            StringBuilder responseMessage = new StringBuilder();
            InputStreamReader reader = new InputStreamReader(response.getEntity().getContent(), HTTP.UTF_8);
            try {
                char[] buffer = new char[4 * 1024];
                int readed = 0;
                while ((readed = reader.read(buffer, 0, buffer.length)) > 0){
                    responseMessage.append(buffer, 0, readed);
                }
            } finally {
                reader.close();
            }

            return responseMessage.toString();
		} finally {
			try {
				if (hcon != null)
					hcon.getConnectionManager().shutdown();
				if (dis != null)
					dis.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	private HttpUriRequest getRequest(int method, String url, String data) throws UnsupportedEncodingException {
		switch (method) {
		case METHOD_GET:
            return new HttpGet(url);
		case METHOD_POST:
			HttpPost post = new HttpPost(url);
			if (data != null) {
				post.setEntity(new StringEntity(data));
			}
			return post;
		case METHOD_PUT:
			HttpPut put = new HttpPut(url);
			if (data != null) {
				put.setEntity(new StringEntity(data));
			}
			return put;
		case METHOD_DELETE:
            return new HttpDelete(url);
		default:
			return null;
		}
	}

}