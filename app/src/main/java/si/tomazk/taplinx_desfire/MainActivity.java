package si.tomazk.taplinx_desfire;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.nxp.nfclib.CardType;
import com.nxp.nfclib.NxpNfcLib;
import com.nxp.nfclib.desfire.DESFireFactory;
import com.nxp.nfclib.exceptions.NxpNfcLibException;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import java.util.UUID;

import si.tomazk.taplinx_desfire.adapters.SectionsPagerAdapter;
import si.tomazk.taplinx_desfire.ui.fragments.ReadFragment;
import si.tomazk.taplinx_desfire.ui.fragments.WriteFragment;

import static com.nxp.nfclib.CardType.DESFireEV1;
import static com.nxp.nfclib.CardType.DESFireEV2;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    public static final int READ_CARD = 0;
    public static final int WRITE_CARD = 1;

    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private NxpNfcLib mLibInstance = null;
    private IntentFilter[] mFilters;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private StringBuilder stringBuilder = new StringBuilder();
    private Object mString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        mViewPager = findViewById(R.id.view_pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(mViewPager);
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        initializeLibrary();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onResume() {
        super.onResume();


        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String uniqueID = UUID.randomUUID().toString();

        mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, null);
        mLibInstance.startForeGroundDispatch();
    }

    @Override
    public void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
        mLibInstance.stopForeGroundDispatch();
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        Log.d(TAG, "onNewIntent");
        stringBuilder.delete(0, stringBuilder.length());
        final Bundle extras = intent.getExtras();
        mString = extras.get("android.nfc.extra.TAG");
        super.onNewIntent(intent);
        try
        {
          cardLogic(intent, mLibInstance);
        }
        catch (Exception e)
        {

        }
    }

    public void cardLogic(final Intent intent, NxpNfcLib libInstance) {

        byte[] cardUid = {0x00,0x00,0x00,0x00,0x00,0x00,0x00};
        Pair<Boolean, String> status = new Pair<>(false, "");

        CardType type = CardType.UnknownCard;
        try {
            type = libInstance.getCardType(intent);
            if (type == CardType.UnknownCard) {
                Toast.makeText(this, getString(R.string.unknown_tag), Toast.LENGTH_LONG).show();
            }
        } catch (NxpNfcLibException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }

        if(type == DESFireEV1 || type == DESFireEV2)
        {
            switch(mViewPager.getCurrentItem()) {
                case READ_CARD:
                    ReadFragment readFragment = (ReadFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, READ_CARD);
                    readFragment.readCard(type, mLibInstance, DESFireFactory.getInstance());
                    break;
                case WRITE_CARD:
                    WriteFragment writeFragment = (WriteFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, WRITE_CARD);
                    writeFragment.writeCard(type, mLibInstance, DESFireFactory.getInstance());
                    break;
                default:
                    break;
            }
        }
        else
        {
            Toast.makeText(this, getString(R.string.unsupported_tag), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeLibrary() {
        final String packageKey = "446b961757d32d0a3496f9a33b5ab543";
        final String packageKeyOffline = "Ave+dWARxgo4Mk01QSHdRwmb2D1e5V/uGanAfnBN/hL0fhcvP89jMP3bWvFVwZTNsosfvgVwpyuU/Y3KWJy2xg==";
        mLibInstance = NxpNfcLib.getInstance();
        try {
            mLibInstance.registerActivity(this, packageKey, packageKeyOffline);
        } catch (NxpNfcLibException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            // do nothing added to handle the crash if any
        }
    }
}