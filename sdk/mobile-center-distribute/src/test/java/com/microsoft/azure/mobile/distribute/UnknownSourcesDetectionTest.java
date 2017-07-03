package com.microsoft.azure.mobile.distribute;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.Arrays;

import static com.microsoft.azure.mobile.distribute.InstallerUtils.INSTALL_NON_MARKET_APPS_ENABLED;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@SuppressLint("InlinedApi")
@SuppressWarnings("deprecation")
@RunWith(PowerMockRunner.class)
@PrepareForTest({Build.class, Settings.Global.class, Settings.Secure.class})
public class UnknownSourcesDetectionTest {
    
    @Mock
    private Context mContext;

    @Mock
    private PackageManager mPackageManager;

    private static void mockApiLevel(int apiLevel) {
        Whitebox.setInternalState(Build.VERSION.class, "SDK_INT", apiLevel);
    }

    @Before
    public void setUp() {
        mockStatic(Settings.Global.class);
        mockStatic(Settings.Secure.class);
        when(mContext.getPackageManager()).thenReturn(mPackageManager);
    }

    @Test
    public void unknownSourcesEnabledViaPackageManager() {
        when(Settings.Secure.getString(any(ContentResolver.class), eq(Settings.Secure.INSTALL_NON_MARKET_APPS))).thenReturn(null);
        when(Settings.Global.getString(any(ContentResolver.class), eq(Settings.Global.INSTALL_NON_MARKET_APPS))).thenReturn(null);
        when(mPackageManager.canRequestPackageInstalls()).thenReturn(true);
        for (int apiLevel = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1; apiLevel < Build.VERSION_CODES.JELLY_BEAN_MR1; apiLevel++) {
            mockApiLevel(apiLevel);
            assertFalse(InstallerUtils.isUnknownSourcesEnabled(mContext));
            verify(mPackageManager, never()).canRequestPackageInstalls();
        }
        for (int apiLevel = Build.VERSION_CODES.JELLY_BEAN_MR1; apiLevel < Build.VERSION_CODES.LOLLIPOP; apiLevel++) {
            mockApiLevel(apiLevel);
            assertFalse(InstallerUtils.isUnknownSourcesEnabled(mContext));
            verify(mPackageManager, never()).canRequestPackageInstalls();
        }
        for (int apiLevel = Build.VERSION_CODES.LOLLIPOP; apiLevel < Build.VERSION_CODES.O; apiLevel++) {
            mockApiLevel(apiLevel);
            assertFalse(InstallerUtils.isUnknownSourcesEnabled(mContext));
            verify(mPackageManager, never()).canRequestPackageInstalls();
        }
        for (int apiLevel = Build.VERSION_CODES.O; apiLevel <= Build.VERSION_CODES.O; apiLevel++) {
            mockApiLevel(apiLevel);
            assertTrue(InstallerUtils.isUnknownSourcesEnabled(mContext));
            verify(mPackageManager).canRequestPackageInstalls();
            reset(mPackageManager);
        }
    }

    @Test
    public void unknownSourcesEnabledViaSystemSecure() {
        when(Settings.Secure.getString(any(ContentResolver.class), eq(Settings.Secure.INSTALL_NON_MARKET_APPS))).thenReturn(INSTALL_NON_MARKET_APPS_ENABLED);
        when(Settings.Global.getString(any(ContentResolver.class), eq(Settings.Global.INSTALL_NON_MARKET_APPS))).thenReturn(null);
        when(mPackageManager.canRequestPackageInstalls()).thenReturn(false);
        for (int apiLevel = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1; apiLevel < Build.VERSION_CODES.JELLY_BEAN_MR1; apiLevel++) {
            mockApiLevel(apiLevel);
            assertTrue(InstallerUtils.isUnknownSourcesEnabled(mContext));
            verify(mPackageManager, never()).canRequestPackageInstalls();
        }
        for (int apiLevel = Build.VERSION_CODES.JELLY_BEAN_MR1; apiLevel < Build.VERSION_CODES.LOLLIPOP; apiLevel++) {
            mockApiLevel(apiLevel);
            assertFalse(InstallerUtils.isUnknownSourcesEnabled(mContext));
            verify(mPackageManager, never()).canRequestPackageInstalls();
        }
        for (int apiLevel = Build.VERSION_CODES.LOLLIPOP; apiLevel < Build.VERSION_CODES.O; apiLevel++) {
            mockApiLevel(apiLevel);
            assertTrue(InstallerUtils.isUnknownSourcesEnabled(mContext));
            verify(mPackageManager, never()).canRequestPackageInstalls();
        }
        for (int apiLevel = Build.VERSION_CODES.O; apiLevel <= Build.VERSION_CODES.O; apiLevel++) {
            mockApiLevel(apiLevel);
            assertFalse(InstallerUtils.isUnknownSourcesEnabled(mContext));
            verify(mPackageManager).canRequestPackageInstalls();
            reset(mPackageManager);
        }
    }

    @Test
    public void unknownSourcesEnabledViaSystemGlobal() {
        when(Settings.Global.getString(any(ContentResolver.class), eq(Settings.Global.INSTALL_NON_MARKET_APPS))).thenReturn(INSTALL_NON_MARKET_APPS_ENABLED);
        when(Settings.Secure.getString(any(ContentResolver.class), eq(Settings.Secure.INSTALL_NON_MARKET_APPS))).thenReturn(null);
        when(mPackageManager.canRequestPackageInstalls()).thenReturn(false);
        for (int apiLevel = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1; apiLevel < Build.VERSION_CODES.JELLY_BEAN_MR1; apiLevel++) {
            mockApiLevel(apiLevel);
            assertFalse(InstallerUtils.isUnknownSourcesEnabled(mContext));
            verify(mPackageManager, never()).canRequestPackageInstalls();
        }
        for (int apiLevel = Build.VERSION_CODES.JELLY_BEAN_MR1; apiLevel < Build.VERSION_CODES.LOLLIPOP; apiLevel++) {
            mockApiLevel(apiLevel);
            assertTrue(InstallerUtils.isUnknownSourcesEnabled(mContext));
            verify(mPackageManager, never()).canRequestPackageInstalls();
        }
        for (int apiLevel = Build.VERSION_CODES.LOLLIPOP; apiLevel < Build.VERSION_CODES.O; apiLevel++) {
            mockApiLevel(apiLevel);
            assertFalse(InstallerUtils.isUnknownSourcesEnabled(mContext));
            verify(mPackageManager, never()).canRequestPackageInstalls();
        }
        for (int apiLevel = Build.VERSION_CODES.O; apiLevel <= Build.VERSION_CODES.O; apiLevel++) {
            mockApiLevel(apiLevel);
            assertFalse(InstallerUtils.isUnknownSourcesEnabled(mContext));
            verify(mPackageManager).canRequestPackageInstalls();
            reset(mPackageManager);
        }
    }

    @Test
    public void disabledAndInvalidValues() {
        for (String value : Arrays.asList(null, "", "0", "on", "true", "TRUE")) {
            when(Settings.Global.getString(any(ContentResolver.class), eq(Settings.Global.INSTALL_NON_MARKET_APPS))).thenReturn(value);
            when(Settings.Secure.getString(any(ContentResolver.class), eq(Settings.Secure.INSTALL_NON_MARKET_APPS))).thenReturn(value);
            for (int apiLevel = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1; apiLevel < Build.VERSION_CODES.O; apiLevel++) {
                mockApiLevel(apiLevel);
                assertFalse(InstallerUtils.isUnknownSourcesEnabled(mContext));
            }
        }
    }
}
