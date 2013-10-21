package com.mmann.notetaking;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.util.Log;

import mmann.sslserver.SocketConnection;
import mmann.sslserver.SocketConnectionListener;

public class AndroidSocketConnection {
	
	SocketConnection connection;
	SocketConnectionListener listener;
	
	/**None of the parameters may be null.*/
	public AndroidSocketConnection(String host, SocketConnectionListener listener, Context context) {
		KeyStore localTrustStore = getKeyStoreFromFileWithPassword(R.raw.client, "password", context);
		SSLContext sslContext = getContextForTrustStore(localTrustStore);
		
		this.connection = new SocketConnection(host, mmann.sslserver.Server.DEFAULT_PORT, sslContext, listener);
	}
	
	private KeyStore getKeyStoreFromFileWithPassword(int id, String password, Context context) {
		KeyStore localTrustStore = null;
		try {
			localTrustStore = KeyStore.getInstance("BKS");
			localTrustStore.load(context.getResources().openRawResource(id), password.toCharArray());
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return localTrustStore;
	}
	
	private SSLContext getContextForTrustStore(KeyStore localTrustStore) {
		SSLContext sslContext = null;
		try {
			TrustManagerFactory tmf = TrustManagerFactory
					.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(localTrustStore);

			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, tmf.getTrustManagers(), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sslContext;
	}
	
	public void start() {
		this.connection.beginListening();
	}
	
	public void end() {
		this.connection.stopListening();
	}
	
	public void writeObject(Object obj) {
		this.connection.writeObject(obj);
	}
	
	public void close() {
		this.connection.stopListening();
	}

}
