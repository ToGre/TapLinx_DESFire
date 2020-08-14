package si.tomazk.taplinx_desfire.ui.fragments;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.nxp.nfclib.CardType;
import com.nxp.nfclib.KeyType;
import com.nxp.nfclib.NxpNfcLib;
import com.nxp.nfclib.defaultimpl.KeyData;
import com.nxp.nfclib.desfire.DESFireFactory;
import com.nxp.nfclib.desfire.IDESFireEV1;
import com.nxp.nfclib.desfire.IDESFireEV2;
import com.nxp.nfclib.utils.NxpLogUtils;
import com.nxp.nfclib.utils.Utilities;

import java.io.File;
import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import si.tomazk.taplinx_desfire.R;

import static si.tomazk.taplinx_desfire.SampleAppKeys.MY_APP;
import static si.tomazk.taplinx_desfire.SampleAppKeys.DEFAULT_KEY_3DES;
import static si.tomazk.taplinx_desfire.SampleAppKeys.MY_KEY_AES128;
import static si.tomazk.taplinx_desfire.SampleAppKeys.NEW_KEY_3DES;

public class ReadFragment extends Fragment {

    private static final String TAG = ReadFragment.class.getName();

    // Store instance variables
    private String title;
    private int page;

    TextView mTvLabel;

    // newInstance constructor for creating fragment with arguments
    public static ReadFragment newInstance(int page, String title) {
        ReadFragment fragmentFirst = new ReadFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_read, container, false);
        mTvLabel = (TextView) view.findViewById(R.id.textView_read);
        mTvLabel.setText(page + " -- " + title);
        return view;
    }

    public void readCard(final CardType type, NxpNfcLib libInstance, final DESFireFactory desFireFactory)
    {
        switch(type)
        {
            case DESFireEV1:
                Log.i(TAG, "DESFireEV1!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                break;

            case DESFireEV2:
                Log.i(TAG, "DESFireEV2!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

                IDESFireEV2 desFireEV2 = desFireFactory.getInstance().getDESFireEV2(libInstance.getCustomModules());

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(getString(R.string.Card_Detected)).append(desFireEV2.getType().getTagName());
                stringBuilder.append("\n\n");

                mTvLabel.setText(stringBuilder.toString());

                if(DESFireEV2Auth(desFireEV2, mTvLabel) == 1)
                {
                    desfireEV2ReadCardLogic(desFireEV2, mTvLabel);
                }

                break;
        }
    }

    private int DESFireEV2Auth(IDESFireEV2 desFireEV2, TextView log) {

        KeyData myAesKeyData = new KeyData();
        myAesKeyData.setKey(new SecretKeySpec(MY_KEY_AES128, "AES"));

        StringBuilder stringBuilder = new StringBuilder();

        try {
            stringBuilder.append(getString(R.string.Selecting_PICC));
            stringBuilder.append("\n");
            desFireEV2.selectApplication(0);

            stringBuilder.append(getString(R.string.PICC_selection_success));
            stringBuilder.append("\n");

            stringBuilder.append(getString(R.string.Auth_with_custom_aes_key));
            stringBuilder.append("\n");

            desFireEV2.authenticate(0, IDESFireEV1.AuthType.AES, KeyType.AES128, myAesKeyData);
            stringBuilder.append(getString(R.string.Auth_with_custom_aes_key_success));
            stringBuilder.append("\n\n");


        } catch (Exception e) {
            stringBuilder.append("-----Exception------");
            stringBuilder.append("\n\n");
            stringBuilder.append(e.getMessage());
            stringBuilder.append("\n\n");
            log.append(stringBuilder.toString());
            return 0;
        }

        log.append(stringBuilder.toString());
        return  1;
    }


    private void desfireEV2ReadCardLogic(IDESFireEV2 desFireEV2, TextView log) {
        int fileSize = 100;
        int timeOut = 2000;
        int fileNo = 0;

        KeyData defDesKeyData = new KeyData();
        defDesKeyData.setKey(new SecretKeySpec(DEFAULT_KEY_3DES, "DESede"));

        StringBuilder stringBuilder = new StringBuilder();
        try {

            stringBuilder.append(getString(R.string.Selecting_MY_APP));
            stringBuilder.append("\n");
            desFireEV2.selectApplication(MY_APP);
            stringBuilder.append(getString(R.string.MY_APP_selection_success));
            stringBuilder.append("\n");

            stringBuilder.append(getString(R.string.Auth_with_default_key));
            stringBuilder.append("\n");

            desFireEV2.authenticate(0, IDESFireEV1.AuthType.Native, KeyType.THREEDES, defDesKeyData);
            stringBuilder.append(getString(R.string.Auth_with_default_key_success));
            stringBuilder.append("\n");

            stringBuilder.append("\n");
            stringBuilder.append(getString(R.string.Data_read_from_the_card)).append(Utilities.dumpBytes(desFireEV2.readData(0, 0, 15)));
            stringBuilder.append("\n\n");

            desFireEV2.getReader().close();
            // Set the custom path where logs will get stored, here we are setting the log folder
            // DESFireLogs under external storage.
            String spath = Environment.getExternalStorageDirectory().getPath() + File.separator + getString(R.string.DESFireLogs);
            NxpLogUtils.setLogFilePath(spath);
            // if you don't call save as below , logs will not be saved.
        } catch (Exception e) {
            stringBuilder.append("-----Exception------");
            stringBuilder.append("\n\n");
            stringBuilder.append(e.getMessage());
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.Unable_to_read));
            stringBuilder.append("\n\n");
        }

        mTvLabel.append(stringBuilder.toString());
        NxpLogUtils.save();
    }
}
