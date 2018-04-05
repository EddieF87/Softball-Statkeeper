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

import static xyz.sleekstats.softball.data.FirestoreHelperService.LEAGUE_COLLECTION;

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
        Log.d("godzilla", "firetaskloader");

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();

        FirebaseFirestore firestore;
        firestore = FirebaseFirestore.getInstance();
        try {
            firestore.setFirestoreSettings(settings);
        } catch (Exception e) {
            Log.d("godzilla", "catch (Exception  " + e.toString());
            Log.e("zztop", e.toString());
        }

        Task<QuerySnapshot> task;
        try {
            task = firestore.collection(LEAGUE_COLLECTION)
                .whereLessThan(userId, 99)
                .get();
            Tasks.await(task);

            if(task.isSuccessful()) {
                Log.d("godzilla", "taskisSuccessful");
                if(isLoadInBackgroundCanceled()) {
                    Log.d("godzilla", "isLoadInBackgroundCanceled");
                    return null;
                }
                Log.d("godzilla", "taskisSuccessfultask.getResult");
                return task.getResult();
            } else {
                Log.d("godzilla", "taskFAIL");
                return null;
            }

        } catch (ExecutionException e) {
            e.printStackTrace();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//
//        while (!task.isComplete()) {
//        }
        Log.d("godzilla", "return null;");
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
