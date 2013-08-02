package org.example.launch;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import aQute.lib.io.IO;

public class Launcher {

	public static void main(String[] args) throws Exception {
		File storage = new File("fwk");
		if (storage.isDirectory())
			IO.deleteWithException(storage);
		
		Map<String, String> config = new HashMap<String, String>();
		config.put(Constants.FRAMEWORK_STORAGE, "fwk");
		
		Iterator<FrameworkFactory> iter = ServiceLoader.load(FrameworkFactory.class).iterator();
		if (!iter.hasNext()) {
			System.err.println("No OSGi framework on the classpath");
			System.exit(1);
		}
		Framework framework = iter.next().newFramework(config);
		
		try {
			framework.init();
			framework.start();
			
			BundleContext context = framework.getBundleContext();
			
			// Install base bundles
			System.out.println("Starting base bundles...");
			provisionDir(context, new File("base"));
			
			// Install app bundles
			System.out.println("Starting app bundles...");
			List<Bundle> appBundles = provisionDir(context, new File("app"));
			
			// Wait
			System.out.println("App started, waiting 10 seconds (try 'sayHello' command)");
			Thread.sleep(10000);
			
			// Uninstall app
			System.out.println("Uninstalling app bundles...");
			for (Bundle bundle : appBundles)
				bundle.uninstall();
			
			// Reinstall!
			System.out.println("App uninstalled... waiting 1 second");
			Thread.sleep(1000);
			
			System.out.println("Reinstalling app...");
			provisionDir(context, new File("app"));
			
			System.out.println("Successfully reinstalled app");
			
			// Wait
			framework.waitForStop(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("FAILED");
			System.exit(1);
		} finally {
			System.out.println("Terminating");
			System.exit(0);
		}
		
	}

	private static List<Bundle> provisionDir(BundleContext context, File bundleDir) throws BundleException, IllegalArgumentException {
		if (!bundleDir.isDirectory())
			throw new IllegalArgumentException("Not a directory: " + bundleDir.getAbsolutePath());

		File[] bundleFiles = bundleDir.listFiles();
		List<Bundle> bundles = new ArrayList<Bundle>(bundleFiles.length);
		for (File bundleFile : bundleFiles) {
			bundles.add(context.installBundle(bundleFile.toURI().toString()));
		}
		for (Bundle bundle : bundles) {
			if (null == bundle.getHeaders().get(Constants.FRAGMENT_HOST))
				bundle.start();
		}
		return bundles;
	}

}
