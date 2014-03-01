package com.swandev.connectapp;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.google.common.collect.Lists;
import com.swandev.swangame.util.ConnectParams;

public class AndroidSwanService implements SwanService {

	private final Activity activity;
	private final PackageManager packageManager;
	
	
	public AndroidSwanService(Activity activity) {
		this.activity = activity; 
		packageManager = activity.getPackageManager();
	}

	@Override
	public List<String> getAvailableGames() {
		final List<ApplicationInfo> packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
		final List<String> swanApps = Lists.newArrayList();
		for (ApplicationInfo packageInfo : packages) {
			if (packageInfo.packageName.contains("swan")) {
				swanApps.add(packageInfo.packageName);
			}
		}
		return swanApps;
	}

	@Override
	public void switchGame(String game, String nickname, String address) {
		final Intent launch = packageManager.getLaunchIntentForPackage(game);
		launch.putExtra(ConnectParams.NICKNAME, nickname);
		launch.putExtra(ConnectParams.SERVER_ADDRESS, address);
		final List<ResolveInfo> activities = packageManager.queryIntentActivities(launch, 0);
		final boolean isIntentSafe = activities.size() > 0;
		if (isIntentSafe) {
			activity.startActivity(launch);
		} 
	}

}
