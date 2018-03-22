package com.example.android.softballstatkeeper.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import static com.example.android.softballstatkeeper.data.FirestoreHelper.FIREDEBUG;
import static com.example.android.softballstatkeeper.data.FirestoreHelper.LEAGUE_COLLECTION;

/**
 * Created by Eddie on 3/9/2018.
 */

public class FireTaskLoader extends android.support.v4.content.AsyncTaskLoader<QuerySnapshot> {

    private final String userID;
    private boolean loading;

    public FireTaskLoader(@NonNull Context context, String id) {
        super(context);
        userID = id;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
        loading = true;
    }

    @Nullable
    @Override
    public QuerySnapshot loadInBackground() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        Log.d(FIREDEBUG, "ID = " + userId);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        Task<QuerySnapshot> task = firestore.collection(LEAGUE_COLLECTION)
                .whereLessThan(userId, 99)
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(FIREDEBUG, e.toString());
                    }
                }).addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        Log.d(FIREDEBUG, "SUCCESS!");
                    }
                });


        while (!task.isComplete()) {
            if(isLoadInBackgroundCanceled()) {
                return null;
            }
        }

        if(task.isSuccessful()) {
            return task.getResult();
        } else {
            return null;
        }
    }

    @Override
    public boolean isLoadInBackgroundCanceled() {
        return !loading;
    }

    @Override
    public void cancelLoadInBackground() {
        super.cancelLoadInBackground();
        loading = false;
    }
}
