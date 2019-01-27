package com.updates.models;

import android.app.Dialog;
import android.os.Bundle;

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
        builder.setMessage("Update already downloaded. Do you want to flash it?")
                .setPositiveButton("Reboot", (dialog, which) -> OTAUtils.rebootRecovery(requireContext()))
                .setNegativeButton("Cancel", ((dialog, which) -> dialog.dismiss()));
        return builder.create();

    }
}
