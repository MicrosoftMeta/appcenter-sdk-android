package com.microsoft.azure.mobile.updates;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.microsoft.azure.mobile.MobileCenter;
import com.microsoft.azure.mobile.utils.HashUtils;
import com.microsoft.azure.mobile.utils.MobileCenterLog;
import com.microsoft.azure.mobile.utils.NetworkStateHelper;
import com.microsoft.azure.mobile.utils.UUIDUtils;
import com.microsoft.azure.mobile.utils.storage.StorageHelper.PreferencesStorage;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.mockito.Mock;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.powermock.reflect.Whitebox;

import static com.microsoft.azure.mobile.updates.UpdateConstants.INVALID_DOWNLOAD_IDENTIFIER;
import static com.microsoft.azure.mobile.updates.UpdateConstants.PREFERENCE_KEY_DOWNLOAD_ID;
import static com.microsoft.azure.mobile.utils.PrefStorageConstants.KEY_ENABLED;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@SuppressWarnings("WeakerAccess")
@PrepareForTest({Updates.class, PreferencesStorage.class, MobileCenterLog.class, MobileCenter.class, NetworkStateHelper.class, BrowserUtils.class, UUIDUtils.class, ReleaseDetails.class, TextUtils.class, InstallerUtils.class, Toast.class})
public class AbstractUpdatesTest {

    static final String TEST_HASH = HashUtils.sha256("com.contoso:1.2.3:6");

    private static final String UPDATES_ENABLED_KEY = KEY_ENABLED + "_Updates";

    @Rule
    public PowerMockRule mPowerMockRule = new PowerMockRule();

    /**
     * Use a timeout to fail test if deadlocks happen due to a code change.
     */
    @Rule
    public Timeout mGlobalTimeout = Timeout.seconds(10);

    @Mock
    Context mContext;

    @Mock
    PackageManager mPackageManager;

    @Mock
    ApplicationInfo mApplicationInfo;

    @Mock
    AlertDialog.Builder mDialogBuilder;

    @Mock
    AlertDialog mDialog;

    @Mock
    Toast mToast;

    NetworkStateHelper mNetworkStateHelper;

    @Before
    @SuppressLint("ShowToast")
    @SuppressWarnings("ResourceType")
    public void setUp() throws Exception {
        Updates.unsetInstance();
        mockStatic(MobileCenterLog.class);
        mockStatic(MobileCenter.class);
        when(MobileCenter.isEnabled()).thenReturn(true);

        /* First call to com.microsoft.azure.mobile.MobileCenter.isEnabled shall return true, initial state. */
        mockStatic(PreferencesStorage.class);
        when(PreferencesStorage.getBoolean(UPDATES_ENABLED_KEY, true)).thenReturn(true);

        /* Then simulate further changes to state. */
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {

                /* Whenever the new state is persisted, make further calls return the new state. */
                boolean enabled = (Boolean) invocation.getArguments()[1];
                when(PreferencesStorage.getBoolean(UPDATES_ENABLED_KEY, true)).thenReturn(enabled);
                return null;
            }
        }).when(PreferencesStorage.class);
        PreferencesStorage.putBoolean(eq(UPDATES_ENABLED_KEY), anyBoolean());

        /* Default download id when not found. */
        when(PreferencesStorage.getLong(PREFERENCE_KEY_DOWNLOAD_ID, INVALID_DOWNLOAD_IDENTIFIER)).thenReturn(INVALID_DOWNLOAD_IDENTIFIER);

        /* Mock package manager. */
        when(mContext.getPackageName()).thenReturn("com.contoso");
        when(mContext.getApplicationInfo()).thenReturn(mApplicationInfo);
        when(mContext.getPackageManager()).thenReturn(mPackageManager);
        PackageInfo packageInfo = mock(PackageInfo.class);
        when(mPackageManager.getPackageInfo("com.contoso", 0)).thenReturn(packageInfo);
        Whitebox.setInternalState(packageInfo, "versionName", "1.2.3");
        Whitebox.setInternalState(packageInfo, "versionCode", 6);

        /* Mock network. */
        mockStatic(NetworkStateHelper.class);
        mNetworkStateHelper = mock(NetworkStateHelper.class, new Returns(true));
        when(NetworkStateHelper.getSharedInstance(any(Context.class))).thenReturn(mNetworkStateHelper);

        /* Mock some statics. */
        mockStatic(BrowserUtils.class);
        mockStatic(UUIDUtils.class);
        mockStatic(ReleaseDetails.class);
        mockStatic(TextUtils.class);
        mockStatic(InstallerUtils.class);
        when(TextUtils.isEmpty(any(CharSequence.class))).thenAnswer(new Answer<Boolean>() {

            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                CharSequence str = (CharSequence) invocation.getArguments()[0];
                return str == null || str.length() == 0;
            }
        });

        /* Dialog. */
        whenNew(AlertDialog.Builder.class).withAnyArguments().thenReturn(mDialogBuilder);
        when(mDialogBuilder.create()).thenReturn(mDialog);
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                when(mDialog.isShowing()).thenReturn(true);
                return null;
            }
        }).when(mDialog).show();
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                when(mDialog.isShowing()).thenReturn(false);
                return null;
            }
        }).when(mDialog).hide();

        /* Toast. */
        mockStatic(Toast.class);
        when(Toast.makeText(any(Context.class), anyInt(), anyInt())).thenReturn(mToast);
    }
}
