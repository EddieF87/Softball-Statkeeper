package xyz.sleekstats.softball.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.ExecutionException;

import xyz.sleekstats.softball.activities.MainActivity;

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
        } catch (Exception ignored) {
            }

            Task<QuerySnapshot> task;
//        try {
            task = firestore.collection(LEAGUE_COLLECTION)
                .whereLessThan(userId, 99)
                .get();
        try {
            Tasks.await(task);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(task.isSuccessful()) {
                if(isLoadInBackgroundCanceled()) {

                    //todo
//                    Toast.makeText(getContext(), "LoadInBackgroundCanceled", Toast.LENGTH_SHORT).show();
                    return null;
                }

                //todo
//                Toast.makeText(getContext(), "FireTaskLoader is successful", Toast.LENGTH_SHORT).show();
                return task.getResult();
            } else {

                //todo
//                Toast.makeText(getContext(), "FireTaskLoader is NOT successful", Toast.LENGTH_SHORT).show();
                return null;
            }

//        } catch (ExecutionException e) {
////            //todo
////            Toast.makeText(getContext(), "FireTaskLoader Exception  " + e.toString(), Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//
//        } catch (InterruptedException e) {
////            //todo
////            Toast.makeText(getContext(), "FireTaskLoader Exception  " + e.toString(), Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//        }
//        return null;
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
