package com.example.sky.myapplication;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * Created by Devansh on 1/7/2016.
 */
public class SortingViewDialog extends DialogFragment {

    MainActivity ma;
    int songViewMode,i;


    @Override
    public void onActivityCreated(Bundle savedInstance){
        super.onActivityCreated(savedInstance);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        ma = (MainActivity)getActivity();
        songViewMode = ma.getSongViewMode();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Sorting By");
        if(songViewMode == 0) {

            LayoutInflater inflater = getActivity().getLayoutInflater();
            final LinearLayout sortLay = (LinearLayout) inflater.inflate(R.layout.sorting_dialog_layout, null);
            //final RadioButton asc = (RadioButton) sortLay.findViewById(R.id.asc_rb);
            //final RadioButton desc = (RadioButton) sortLay.findViewById(R.id.desc_rb);
            //RadioButton alpha = (RadioButton) sortLay.findViewById(R.id.alpha_rb);
            //RadioButton date = (RadioButton) sortLay.findViewById(R.id.date_rb);
            //RadioButton count = (RadioButton) sortLay.findViewById(R.id.counter_rb);
            // Log.i("Musik"," "+ma);
            final RadioGroup rg = (RadioGroup) sortLay.findViewById(R.id.rg_order);
            //rg.addView(asc);
            //rg.addView(desc);
            final RadioGroup rg2 = (RadioGroup) sortLay.findViewById(R.id.rg_type);
            //rg2.addView(alpha);
            //rg2.addView(date);
            //rg2.addView(count);
            String so = ma.getSortingOrder();
            if (so.equals("ASC"))
                rg.check(R.id.asc_rb);
            else
                rg.check(R.id.desc_rb);
            String st = ma.getSortingType();
            if (st.equals("ALPHA"))
                rg2.check(R.id.alpha_rb);
            else if (st.equals("DATE"))
                rg2.check(R.id.date_rb);
            else
                rg2.check(R.id.counter_rb);

            builder.setView(sortLay);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK, so save the mSelectedItems results somewhere
                    // or return them to the component that opened the dialog
                    //Log.i("Musik","so- "+rg.getCheckedRadioButtonId()+" st- "+rg2.getCheckedRadioButtonId());
                    RadioButton order = (RadioButton) sortLay.findViewById(rg.getCheckedRadioButtonId());
                    RadioButton type = (RadioButton) sortLay.findViewById(rg2.getCheckedRadioButtonId());
                    String o = order.getText().toString();
                    if (o.equals("Ascending"))
                        ma.setSortingOrder("ASC");
                    else
                        ma.setSortingOrder("DESC");
                    String t = type.getText().toString();
                    if (t.equals("Alphabetically"))
                        ma.setSortingType("ALPHA");
                    else if (t.equals("Date Added"))
                        ma.setSortingType("DATE");
                    //Count has not been added yet
                    ma.reSortSongs();
                }
            })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            getDialog().dismiss();
                        }
                    });
        }
        else if(songViewMode == 1 || songViewMode == 2 || songViewMode == 3){
            String so = ma.getSortingOrder();
            if(so.equals("ASC"))
                i=0;
            else
                i=1;
            builder.setSingleChoiceItems(R.array.sort_mode_array, i, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which == 0){
                        ma.setSortingOrder("ASC");
                    }
                    else {
                        ma.setSortingOrder("DESC");
                    }
                    ma.reSortSongs();
                    dialog.dismiss();
                }
            });
        }
        /*
        asc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                asc.toggle();
                if(asc.isChecked())
                    desc.
            }
        });

        desc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                asc.toggle();
                desc.toggle();
                if(desc.isChecked())
                    ma.setSortingOrder("DESC");
            }
        });

        alpha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        */
        return builder.create();

    }

}
