package com.updates.models;

import android.app.Dialog;
import android.os.Bundle;

import com.updates.R;
import com.updates.utils.OTAUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class RebootDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage(getString(R.string.dialog_already_downloaded))
                .setPositiveButton(getString(R.string.dialog_reboot_flash), (dialog, which) -> OTAUtils.rebootRecovery(requireContext()))
                .setNegativeButton(getString(R.string.dialog_cancel_flash), ((dialog, which) -> dialog.dismiss()));
        return builder.create();

    }
}
