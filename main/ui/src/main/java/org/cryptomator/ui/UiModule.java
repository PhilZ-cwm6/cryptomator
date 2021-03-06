/*******************************************************************************
 * Copyright (c) 2016, 2017 Sebastian Stenzel and others.
 * This file is licensed under the terms of the MIT license.
 * See the LICENSE.txt file for more info.
 *
 * Contributors:
 *     Sebastian Stenzel - initial API and implementation
 *******************************************************************************/
package org.cryptomator.ui;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.inject.Named;
import javax.inject.Singleton;

import org.cryptomator.common.CommonsModule;
import org.cryptomator.common.settings.Settings;
import org.cryptomator.common.settings.SettingsProvider;
import org.cryptomator.frontend.webdav.WebDavServer;
import org.cryptomator.jni.JniModule;
import org.cryptomator.keychain.KeychainModule;
import org.cryptomator.ui.controllers.ViewControllerModule;
import org.cryptomator.ui.model.VaultComponent;
import org.fxmisc.easybind.EasyBind;

import dagger.Module;
import dagger.Provides;
import javafx.beans.binding.Binding;

@Module(includes = {ViewControllerModule.class, CommonsModule.class, KeychainModule.class, JniModule.class}, subcomponents = {VaultComponent.class})
public class UiModule {

	@Provides
	@Singleton
	Settings provideSettings(SettingsProvider settingsProvider) {
		return settingsProvider.get();
	}

	@Provides
	@Singleton
	ExecutorService provideExecutorService(@Named("shutdownTaskScheduler") Consumer<Runnable> shutdownTaskScheduler) {
		ExecutorService executorService = Executors.newCachedThreadPool();
		shutdownTaskScheduler.accept(executorService::shutdown);
		return executorService;
	}

	@Provides
	@Singleton
	Binding<InetSocketAddress> provideServerSocketAddressBinding(Settings settings) {
		return EasyBind.combine(settings.useIpv6(), settings.port(), (useIpv6, port) -> {
			String host = useIpv6 ? "::1" : "localhost";
			return InetSocketAddress.createUnresolved(host, port.intValue());
		});
	}

	@Provides
	@Singleton
	WebDavServer provideWebDavServer(Binding<InetSocketAddress> serverSocketAddressBinding) {
		WebDavServer server = WebDavServer.create();
		// no need to unsubscribe eventually, because server is a singleton
		EasyBind.subscribe(serverSocketAddressBinding, server::bind);
		return server;
	}

}
