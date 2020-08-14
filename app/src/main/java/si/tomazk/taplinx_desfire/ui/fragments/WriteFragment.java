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
import com.nxp.nfclib.desfire.IDESFireEV1;
import com.nxp.nfclib.desfire.IDESFireEV2;
import com.nxp.nfclib.utils.NxpLogUtils;
import com.nxp.nfclib.utils.Utilities;
import com.nxp.nfclib.KeyType;

import java.io.File;
import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import si.tomazk.taplinx_desfire.R;

import static si.tomazk.taplinx_desfire.SampleAppKeys.MY_APP;
import static si.tomazk.taplinx_desfire.SampleAppKeys.DEFAULT_KEY_2KTDES;
import static si.tomazk.taplinx_desfire.SampleAppKeys.NEW_KEY_2KTDES;

public class WriteFragment extends Fragment {

    private static final String TAG = WriteFragment.class.getName();

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
                desfireEV2CardLogic(desFireFactory.getInstance().getDESFireEV2(libInstance.getCustomModules()));
                break;
        }
    }

    private void desfireEV2CardLogic(IDESFireEV2 desFireEV2) {
        int fileSize = 100;
        byte[] data = new byte[]{0x11, 0x11, 0x11, 0x11, 0x11};
        int timeOut = 2000;
        int fileNo = 0;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getString(R.string.Card_Detected)).append(desFireEV2.getType().getTagName());
        stringBuilder.append("\n\n");
        try {
            stringBuilder.append(getString(R.string.Selecting_PICC));
            stringBuilder.append("\n\n");
            desFireEV2.selectApplication(0);
            stringBuilder.append(getString(R.string.PICC_selection_success));
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.Auth_with_default_key));
            stringBuilder.append("\n\n");

            Key defKey = new SecretKeySpec(DEFAULT_KEY_2KTDES, "DeSade");
            KeyData defKeyData = new KeyData();
            defKeyData.setKey(defKey);

            desFireEV2.authenticate(0, IDESFireEV1.AuthType.Native, KeyType.THREEDES, defKeyData);
            stringBuilder.append(getString(R.string.Authentication_status_true));
            stringBuilder.append("\n\n");
            desFireEV2.getReader().setTimeout(timeOut);
            if(mFormatCheckBox.isChecked())
            {
                stringBuilder.append(getString(R.string.Formatting_card));
                stringBuilder.append("\n\n");
                desFireEV2.format();
                stringBuilder.append(getString(R.string.Formatted_card_successfully));
                stringBuilder.append("\n\n");
            }
            else
            {
                stringBuilder.append(getString(R.string.Creating_application));
                stringBuilder.append("\n\n");

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
                stringBuilder.append("\n\n");
                desFireEV2.selectApplication(MY_APP);
                desFireEV2.createFile(fileNo, new DESFireFile.StdDataFileSettings(
                        IDESFireEV1.CommunicationType.Plain, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
                        fileSize));

                Key newKey = new SecretKeySpec(NEW_KEY_2KTDES, "DeSade");
                KeyData newKeyData = new KeyData();
                newKeyData.setKey(defKey);

                desFireEV2.authenticate(0, IDESFireEV1.AuthType.Native, KeyType.TWO_KEY_THREEDES, newKeyData);
                stringBuilder.append(getString(R.string.Writing_data_to_tag));
                stringBuilder.append("\n\n");
                stringBuilder.append(getString(R.string.Data_to_write)).append(Utilities.dumpBytes(data));
                stringBuilder.append("\n\n");
                desFireEV2.writeData(0, 0, data);
                stringBuilder.append(getString(R.string.Data_written_successfully));
                stringBuilder.append("\n\n");
                stringBuilder.append(getString(R.string.Data_read_from_the_card)).append(Utilities.dumpBytes(desFireEV2.readData(0, 0, 5)));
                stringBuilder.append("\n\n");
            }

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

        mTvLabel.setText(stringBuilder.toString());
        NxpLogUtils.save();
    }
}
