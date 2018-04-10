package xyz.sleekstats.softball.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.ExecutionException;

import static xyz.sleekstats.softball.data.FirestoreUpdateService.LEAGUE_COLLECTION;

/**
 * Created by Eddie on 3/9/2018.
 */

public class FireTaskLoader extends android.support.v4.content.AsyncTaskLoader<QuerySnapshot> {

    private boolean loading;

    public FireTaskLoader(@NonNull Context context) {
        super(context);
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

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();

        FirebaseFirestore firestore;
        firestore = FirebaseFirestore.getInstance();
        try {
            firestore.setFirestoreSettings(settings);
        } catch (Exception e) {
            Log.e("errorlog", e.toString());
        }

        Task<QuerySnapshot> task;
        try {
            task = firestore.collection(LEAGUE_COLLECTION)
                .whereLessThan(userId, 99)
                .get();
            Tasks.await(task);

            if(task.isSuccessful()) {
                if(isLoadInBackgroundCanceled()) {
                    return null;
                }
                return task.getResult();
            } else {
                return null;
            }

        } catch (ExecutionException e) {
            e.printStackTrace();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
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
