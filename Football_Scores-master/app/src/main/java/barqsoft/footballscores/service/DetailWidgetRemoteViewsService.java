package barqsoft.footballscores.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.lang.annotation.Target;
import java.util.concurrent.ExecutionException;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.scoresAdapter;

/**
 * An {@link RemoteViewsService}  for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    private RemoteViews rootView;
    private Cursor mcursor;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if(mcursor != null){
                    mcursor.close();
                }

                Time t = new Time();
                t.setToNow();
               // int julianDay = Time.getJulianDay(86400000, t.gmtoff);
                int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
                final long identityToken = Binder.clearCallingIdentity();
                rootView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.scores_list_item);
                /*mcursor = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(),
                        new String[]{DatabaseContract.scores_table.MATCH_ID
                                ,DatabaseContract.scores_table.HOME_COL
                                ,DatabaseContract.scores_table.HOME_GOALS_COL
                                ,DatabaseContract.scores_table.AWAY_COL
                                ,DatabaseContract.scores_table.AWAY_GOALS_COL
                                ,DatabaseContract.scores_table.LEAGUE_COL}
                        ,
                        null,
                        new String[]{String.valueOf(currentJulianDay)},
                        DatabaseContract.scores_table.DATE_COL+" DESC");*/
                mcursor = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(),
                        null,
                        null,
                        null,
                        DatabaseContract.scores_table.DATE_COL+" DESC");

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (mcursor != null) {
                    mcursor.close();
                    mcursor = null;
                }
            }

            @Override
            public int getCount() {
                return mcursor == null? 0:mcursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        mcursor == null || !mcursor.moveToPosition(position)) {
                    return null;
                }

                //get data and populate list
                String awayTeam = mcursor.getString(scoresAdapter.COL_AWAY);
                int awayGoals = mcursor.getInt(scoresAdapter.COL_AWAY_GOALS);
                String homeTeam = mcursor.getString(scoresAdapter.COL_HOME);
                int homeGoals = mcursor.getInt(scoresAdapter.COL_HOME_GOALS);
                int league_id = mcursor.getInt(scoresAdapter.COL_LEAGUE);
                int id = mcursor.getInt(scoresAdapter.COL_ID);

                rootView.setTextViewText(R.id.home_name, homeTeam);
                rootView.setTextViewText(R.id.score_textview, Utilies.getScores(homeGoals, awayGoals));
                rootView.setTextViewText(R.id.away_name, awayTeam);
                rootView.setTextViewText(R.id.data_textview, Utilies.getLeague(league_id));

                final Intent fillInIntent = new Intent(getApplicationContext(), MainActivity.class);
                Uri scoresUri = DatabaseContract.scores_table.buildScoreWithDate();
                fillInIntent.setData(scoresUri);
                rootView.setOnClickFillInIntent(R.id.widget_listView, fillInIntent);

                return rootView;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.scores_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if(mcursor != null && mcursor.moveToPosition(position))
                    return mcursor.getLong(scoresAdapter.COL_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
