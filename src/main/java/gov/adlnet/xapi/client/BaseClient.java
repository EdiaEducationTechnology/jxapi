package gov.adlnet.xapi.client;

import gov.adlnet.xapi.model.Actor;
import gov.adlnet.xapi.model.IStatementObject;
import gov.adlnet.xapi.model.adapters.ActorAdapter;
import gov.adlnet.xapi.model.adapters.StatementObjectAdapter;
import gov.adlnet.xapi.util.Base64;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.http.HttpServletResponse;

public class BaseClient {
	protected URL _host;
	protected Gson gson;
	protected String username;
	protected String password;
	protected String authString;
	public BaseClient(String uri, String username, String password)
			throws MalformedURLException {
		init(new URL(uri), username, password);
	}

	public BaseClient(URL uri, String username, String password)
			throws MalformedURLException {
		init(uri, username, password);
	}	
	protected Gson getDecoder() {	
		if (gson == null) {
			GsonBuilder builder = new GsonBuilder();
			builder.registerTypeAdapter(Actor.class, new ActorAdapter());
			builder.registerTypeAdapter(IStatementObject.class,
					new StatementObjectAdapter());
			gson = builder.create();
		}
		return gson;
	}

	protected void init(URL uri, String user, String password) {
		this._host = uri;
		this.username = user;
		this.password = password;
        this.authString = "Basic " + Base64.encodeToString((this.username + ":" + this.password).getBytes(), Base64.NO_WRAP);
    }

	protected String readFromConnection(HttpURLConnection conn)
			throws java.io.IOException {
		InputStream in;
        if(conn.getResponseCode() >= 400){
            in = new BufferedInputStream(conn.getErrorStream());
            StringBuilder sb = new StringBuilder();
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(reader);
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } finally {
                br.close();
                reader.close();
            }
            throw new IOException(String.format("Server Responded with %d: %s",
                    conn.getResponseCode(), sb.toString()));
        }
        else {
            in = new BufferedInputStream(conn.getInputStream());
            StringBuilder sb = new StringBuilder();
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(reader);
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            } finally {
                br.close();
                reader.close();
            }
        }
	}

	protected HttpURLConnection initializeConnection(URL url)
			throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.addRequestProperty("X-Experience-API-Version", "1.0.0");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Authorization", this.authString);
		return conn;
	}
	
	protected HttpURLConnection initializePOSTConnection(URL url)
			throws IOException {
		HttpURLConnection conn = initializeConnection(url);
		conn.setDoOutput(true);
		return conn;
	}

	protected String issuePost(String path, String data)
			throws java.io.IOException {
		String basePath = this._host.getPath() != null ? this._host.getPath() : "";
		if (basePath.endsWith("/")) {
			basePath = basePath.substring(0, basePath.length()-1);
		}
        URL url = new URL(this._host.getProtocol(), this._host.getHost(),this._host.getPort() ,basePath + path);
		HttpURLConnection conn = initializePOSTConnection(url);
		conn.setRequestMethod("POST");
		OutputStreamWriter writer = new OutputStreamWriter(
				conn.getOutputStream());
		try {
			writer.write(data);
		} catch (IOException ex) {
			InputStream s = conn.getErrorStream();
			InputStreamReader isr = new InputStreamReader(s);
			BufferedReader br = new BufferedReader(isr);
			try {
				String line;
				while((line = br.readLine()) != null){
					System.out.print(line);
				}
				System.out.println();
			} finally {
				s.close();
			}
			throw ex;
		} finally {
			writer.close();
		}
		try {
			return readFromConnection(conn);
		} finally {
			conn.disconnect();
		}
	}

    protected String issuePut(String path, String data)
            throws java.io.IOException {
        URL url = new URL(this._host.getProtocol(), this._host.getHost(),this._host.getPort() ,path);
        HttpURLConnection conn = initializePOSTConnection(url);
        conn.setRequestMethod("PUT");
        OutputStreamWriter writer = new OutputStreamWriter(
                conn.getOutputStream());
        try {
            writer.write(data);
        } catch (IOException ex) {
            InputStream s = conn.getErrorStream();
            InputStreamReader isr = new InputStreamReader(s);
            BufferedReader br = new BufferedReader(isr);
            try {
                String line;
                while((line = br.readLine()) != null){
                    System.out.print(line);
                }
                System.out.println();
            } finally {
                s.close();
            }
            throw ex;
        } finally {
            writer.close();
        }
        try {
            return readFromConnection(conn);
        } finally {
            conn.disconnect();
        }
    }

    protected String issueDelete(String path)
            throws java.io.IOException {
        URL url = new URL(this._host.getProtocol(), this._host.getHost(),this._host.getPort() ,path);
        HttpURLConnection conn = initializeConnection(url);
        conn.setRequestMethod("DELETE");
        try{
            return readFromConnection(conn);
        }
        catch (IOException ex){
            InputStream s = conn.getErrorStream();
            InputStreamReader isr = new InputStreamReader(s);
            BufferedReader br = new BufferedReader(isr);
            try {
                String line;
                while((line = br.readLine()) != null){
                    System.out.print(line);
                }
                System.out.println();
            } finally {
                s.close();
            }
            throw ex;
        }
        finally{
            conn.disconnect();
        }
    }

	protected String issueGet(String path) throws java.io.IOException {
		URL url = new URL(this._host.getProtocol(), this._host.getHost(),this._host.getPort() ,path);
        HttpURLConnection conn = initializeConnection(url);
        try {
			return readFromConnection(conn);
		} catch (IOException ex) {
			InputStream s = conn.getErrorStream();
			InputStreamReader isr = new InputStreamReader(s);
			BufferedReader br = new BufferedReader(isr);
			try {
				String line;
				while((line = br.readLine()) != null){
					System.out.print(line);
				}
				System.out.println();
			} finally {
				s.close();
			}
			throw ex;
		}finally {
			conn.disconnect();
		}
	}

    protected HttpServletResponse issueGetWithAttachments(String path) throws java.io.IOException {
        URL url = new URL(this._host.getProtocol(), this._host.getHost(),this._host.getPort() ,path);
        HttpURLConnection conn = initializeConnection(url);
        try {
            return (HttpServletResponse)conn.getInputStream();
        } catch (IOException ex) {
            InputStream s = conn.getErrorStream();
            InputStreamReader isr = new InputStreamReader(s);
            BufferedReader br = new BufferedReader(isr);
            try {
                String line;
                while((line = br.readLine()) != null){
                    System.out.print(line);
                }
                System.out.println();
            } finally {
                s.close();
            }
            throw ex;
        }finally {
            conn.disconnect();
        }
    }
}
