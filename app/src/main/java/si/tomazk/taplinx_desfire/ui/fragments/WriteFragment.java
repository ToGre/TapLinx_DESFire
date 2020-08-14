package si.tomazk.taplinx_desfire.ui.fragments;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.nxp.nfclib.CardType;
import com.nxp.nfclib.NxpNfcLib;
import com.nxp.nfclib.defaultimpl.KeyData;
import com.nxp.nfclib.desfire.DESFireFactory;
import com.nxp.nfclib.desfire.DESFireFile;
import com.nxp.nfclib.desfire.EV1ApplicationKeySettings;
import com.nxp.nfclib.desfire.EV1PICCKeySettings;
import com.nxp.nfclib.desfire.IDESFireEV1;
import com.nxp.nfclib.desfire.IDESFireEV2;
import com.nxp.nfclib.utils.NxpLogUtils;
import com.nxp.nfclib.utils.Utilities;
import com.nxp.nfclib.KeyType;

import java.io.File;

import javax.crypto.spec.SecretKeySpec;

import si.tomazk.taplinx_desfire.R;

import static si.tomazk.taplinx_desfire.SampleAppKeys.MY_APP;
import static si.tomazk.taplinx_desfire.SampleAppKeys.DEFAULT_KEY_3DES;
import static si.tomazk.taplinx_desfire.SampleAppKeys.MY_KEY_AES128;

public class WriteFragment extends Fragment {

    private static final String TAG = WriteFragment.class.getName();

    private static final int UNKNOWN_PICC_KEY = 0;
    private static final int DEFAULT_PICC_KEY = 1;
    private static final int MY_PICC_KEY = 2;

    // Store instance variables
    private String title;
    private int page;

    private TextView mTvLabel;
    private CheckBox mFormatCheckBox;

    private boolean bWriteAllowed = true;

    // newInstance constructor for creating fragment with arguments
    public static WriteFragment newInstance(int page, String title) {
        WriteFragment fragmentFirst = new WriteFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_write, container, false);
        mTvLabel = (TextView) view.findViewById(R.id.textView_write);
        mTvLabel.setText(page + " -- " + title);

        mFormatCheckBox = (CheckBox) view.findViewById(R.id.checkBoxFormat);

        return view;
    }

    public void writeCard(final CardType type, NxpNfcLib libInstance, final DESFireFactory desFireFactory)
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

                int cardType = DESFireEV2Auth(desFireEV2, mTvLabel);

                if(cardType == MY_PICC_KEY || cardType == DEFAULT_PICC_KEY)
                {
                    if(mFormatCheckBox.isChecked())
                    {
                        DESFireEV2CardFormattingLogic(desFireEV2, mTvLabel, cardType);
                    }
                    else
                    {
                        DESFireEV2CardLogic(desFireEV2, mTvLabel,cardType);
                    }
                }

                break;
        }
    }

    private int DESFireEV2CardFormattingLogic(IDESFireEV2 desFireEV2, TextView log, int cardType) {

        StringBuilder stringBuilder = new StringBuilder();

        try {

            stringBuilder.append(getString(R.string.Formatting_card));
            stringBuilder.append("\n");

            desFireEV2.format();
            stringBuilder.append(getString(R.string.Formatted_card_success));
            stringBuilder.append("\n");

            if(cardType == MY_PICC_KEY)
            {
                stringBuilder.append(getString(R.string.Change_PICC_key));
                stringBuilder.append("\n");

                desFireEV2.changeKey(0,KeyType.THREEDES, MY_KEY_AES128, DEFAULT_KEY_3DES, (byte) 1);

                stringBuilder.append(getString(R.string.Change_PICC_key_success));
                stringBuilder.append("\n");
            }
            stringBuilder.append("\n");
        } catch (Exception e) {
            stringBuilder.append("-----Exception------");
            stringBuilder.append("\n\n");
            stringBuilder.append(e.getMessage());
            stringBuilder.append("\n\n");
            return 0;
        }

        log.append(stringBuilder.toString());
        return 1;
    }

    private int DESFireEV2Auth(IDESFireEV2 desFireEV2, TextView log) {

        KeyData defDesKeyData = new KeyData();
        defDesKeyData.setKey(new SecretKeySpec(DEFAULT_KEY_3DES, "DESede"));

        KeyData myAesKeyData = new KeyData();
        myAesKeyData.setKey(new SecretKeySpec(MY_KEY_AES128, "AES"));

        StringBuilder stringBuilder = new StringBuilder();

        try {
            stringBuilder.append(getString(R.string.Selecting_PICC));
            stringBuilder.append("\n");
            desFireEV2.selectApplication(0);

            stringBuilder.append(getString(R.string.PICC_selection_success));
            stringBuilder.append("\n");

            stringBuilder.append(getString(R.string.Auth_with_default_key));
            stringBuilder.append("\n");

            desFireEV2.authenticate(0, IDESFireEV1.AuthType.Native, KeyType.THREEDES, defDesKeyData);
            stringBuilder.append(getString(R.string.Auth_with_default_key_success));
            stringBuilder.append("\n\n");

            log.append(stringBuilder.toString());
            return  DEFAULT_PICC_KEY;

        } catch (Exception e) {
            stringBuilder.append("-----Exception------");
            stringBuilder.append("\n\n");
            stringBuilder.append(e.getMessage());
            stringBuilder.append("\n\n");
        }

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

            log.append(stringBuilder.toString());
            return  MY_PICC_KEY;

        } catch (Exception e) {
            stringBuilder.append("-----Exception------");
            stringBuilder.append("\n\n");
            stringBuilder.append(e.getMessage());
            stringBuilder.append("\n\n");
        }

        log.append(stringBuilder.toString());
        return  UNKNOWN_PICC_KEY;
    }


    private void DESFireEV2CardLogic(IDESFireEV2 desFireEV2, TextView log, int cardType) {
        int fileSize = 100;
        byte[] data = new byte[]{0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte)0x88, (byte)0x99, (byte)0xAA, (byte)0xBB, (byte)0xCC, (byte)0xDD, (byte)0xEE, (byte)0xFF};
        int timeOut = 2000;
        int fileNo = 0;

        KeyData defDesKeyData = new KeyData();
        defDesKeyData.setKey(new SecretKeySpec(DEFAULT_KEY_3DES, "DESede"));

        KeyData myAesKeyData = new KeyData();
        myAesKeyData.setKey(new SecretKeySpec(MY_KEY_AES128, "AES"));

        StringBuilder stringBuilder = new StringBuilder();

        try {
            if(cardType == DEFAULT_PICC_KEY) //if this default format card
            {
                stringBuilder.append(getString(R.string.Change_PICC_key));
                stringBuilder.append("\n");

                desFireEV2.getReader().setTimeout(timeOut);
                desFireEV2.changeKey(0,KeyType.AES128, DEFAULT_KEY_3DES, MY_KEY_AES128, (byte) 1);

                stringBuilder.append(getString(R.string.Change_PICC_key_success));
                stringBuilder.append("\n");

                stringBuilder.append(getString(R.string.Auth_with_custom_aes_key));
                stringBuilder.append("\n");

                desFireEV2.authenticate(0, IDESFireEV1.AuthType.AES, KeyType.AES128, myAesKeyData);
                stringBuilder.append(getString(R.string.Auth_with_custom_aes_key_success));
                stringBuilder.append("\n");

                //FOR PRODUCTION
                EV1PICCKeySettings.Builder piccsetbuilder = new EV1PICCKeySettings.Builder();
                EV1PICCKeySettings piccsettings = piccsetbuilder.setPiccMasterKeyChangeable(true)
                        .setAuthenticationRequiredForApplicationManagement(true)
                        .setAuthenticationRequiredForDirectoryConfigurationData(false).build(); //FOR PRODUCTION set this on true
                desFireEV2.changeKeySettings(piccsettings);

                stringBuilder.append(getString(R.string.Creating_application));
                stringBuilder.append("\n");

                EV1ApplicationKeySettings.Builder appsetbuilder = new EV1ApplicationKeySettings.Builder();
                EV1ApplicationKeySettings appsettings = appsetbuilder.setAppKeySettingsChangeable(
                        true).setAppMasterKeyChangeable(true)
                        .setAuthenticationRequiredForFileManagement(false)
                        .setAuthenticationRequiredForDirectoryConfigurationData(
                                false).setKeyTypeOfApplicationKeys(
                                KeyType.TWO_KEY_THREEDES).build();
                desFireEV2.createApplication(MY_APP, appsettings);
                stringBuilder.append(getString(R.string.App_creation_success)).append(
                        Utilities.dumpBytes(MY_APP));
                stringBuilder.append("\n");
                stringBuilder.append(getString(R.string.Selecting_MY_APP));
                stringBuilder.append("\n");
                desFireEV2.selectApplication(MY_APP);
                stringBuilder.append(getString(R.string.MY_APP_selection_success));
                stringBuilder.append("\n");

                stringBuilder.append(getString(R.string.Creating_file));
                stringBuilder.append("\n");
                desFireEV2.createFile(fileNo, new DESFireFile.StdDataFileSettings(
                        IDESFireEV1.CommunicationType.Plain, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
                        fileSize));
                stringBuilder.append(getString(R.string.Creating_file_success));
                stringBuilder.append("\n");

                stringBuilder.append(getString(R.string.Auth_with_default_key));
                stringBuilder.append("\n");

                desFireEV2.authenticate(0, IDESFireEV1.AuthType.Native, KeyType.THREEDES, defDesKeyData);
                stringBuilder.append(getString(R.string.Auth_with_default_key_success));
                stringBuilder.append("\n");
            }
            else    //this is my card format
            {
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
            }


            stringBuilder.append(getString(R.string.Writing_data_to_file));
            stringBuilder.append("\n");
            stringBuilder.append(getString(R.string.Data_to_write)).append(Utilities.dumpBytes(data));
            stringBuilder.append("\n");
            desFireEV2.writeData(0, 0, data);
            stringBuilder.append(getString(R.string.Data_written_successfully));
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

