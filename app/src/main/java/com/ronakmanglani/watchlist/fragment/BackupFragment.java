package com.ronakmanglani.watchlist.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.ronakmanglani.watchlist.R;
import com.ronakmanglani.watchlist.Watchlist;
import com.ronakmanglani.watchlist.util.FileUtils;

import java.io.File;

public class BackupFragment extends PreferenceFragment {

    private static final String DATABASE_NAME = "movieDatabase.db";

    private static final int BACKUP_CODE = 42;
    private static final int RESTORE_CODE = 43;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Watchlist.TABLE_USER);
        addPreferencesFromResource(R.xml.pref_backup);

        Preference backupPreference = findPreference(getString(R.string.pref_key_backup));
        backupPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(getActivity(), FilePickerActivity.class);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
                startActivityForResult(i, BACKUP_CODE);
                Toast.makeText(getActivity(), R.string.backup_select, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        Preference restorePreference = findPreference(getString(R.string.pref_key_restore));
        restorePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(getActivity(), FilePickerActivity.class);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
                startActivityForResult(i, RESTORE_CODE);
                Toast.makeText(getActivity(), R.string.restore_select, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BACKUP_CODE && resultCode == Activity.RESULT_OK) {
            try {
                File srcFile = getActivity().getDatabasePath(DATABASE_NAME);
                File dstFile = new File(data.getData().getPath() + File.separator
                        + "Watchlist-" + (System.currentTimeMillis()/1000) + ".wbk");
                FileUtils.copyFile(srcFile, dstFile);
                Toast.makeText(getActivity(), R.string.backup_complete, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getActivity(), R.string.backup_failed, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == RESTORE_CODE && resultCode == Activity.RESULT_OK) {
            File srcFile = new File(data.getData().getPath());
            if (FileUtils.isValidDbFile(srcFile)) {
                try {
                    File dstFile = getActivity().getDatabasePath(DATABASE_NAME);
                    FileUtils.copyFile(srcFile, dstFile);
                    Toast.makeText(getActivity(), R.string.restore_complete, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getActivity(), R.string.restore_failed, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), R.string.restore_invalid, Toast.LENGTH_SHORT).show();
            }
        }
    }
}