package com.example.collegescope;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

public class EditActivity extends AppCompatActivity
{
    private EditText newUserName;
    private TextView newUserSAT, newUserGPA;
    private SeekBar sbSAT, sbGPA;
    private Button save;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        newUserName = findViewById(R.id.etNameUpdate);
        newUserGPA = findViewById(R.id.tvEditGPA);
        newUserSAT = findViewById(R.id.tvEditSAT);
        save = findViewById(R.id.btnSave);
        sbSAT = findViewById(R.id.sbEditSAT);
        sbGPA = findViewById(R.id.sbEditGPA);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        if(sbSAT != null)
        {
            sbSAT.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                {
                    int stepSize = 10;
                    sbSAT.setMax(1200);

                    progress = (progress/stepSize) * stepSize;
                    seekBar.setProgress(progress);

                    int scoreSATNum = progress + 400;
                    String SATScore = String.valueOf(scoreSATNum);

                    newUserSAT.setText("SAT: " + SATScore);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

        if(sbGPA != null)
        {
            sbGPA.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                {
                    sbGPA.setMax(400);

                    seekBar.setProgress(progress);

                    double GPANum = (double)progress / 100;
                    String GPA = String.valueOf(GPANum);

                    newUserGPA.setText("GPA: " + GPA);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar)
                {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar)
                {

                }
            });

        }

        final DatabaseReference databaseReference = firebaseDatabase.getReference(firebaseAuth.getUid());

        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                newUserName.setText(userProfile.getUserName());
                newUserGPA.setText("GPA: " + userProfile.getUserGPA());
                newUserSAT.setText("SAT: " + userProfile.getUserSAT());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        save.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String name = newUserName.getText().toString();
                String SAT = newUserSAT.getText().toString().substring(5);
                String GPA = newUserGPA.getText().toString().substring(5);

                ProfileActivity profileActivity = new ProfileActivity();
                String email = profileActivity.getOriginalEmail();

                UserProfile userProfile = new UserProfile(SAT, email, name, GPA);

                databaseReference.setValue(userProfile);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
