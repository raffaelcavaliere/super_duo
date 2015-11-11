package barqsoft.footballscores.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;

/**
 * Created by raffaelcavaliere on 2015-11-10.
 */
public class ScoresWidgetIntentService extends IntentService {

    private final String[] columns = {
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.LEAGUE_COL
    };

    public ScoresWidgetIntentService() {
        super("ScoresWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, ScoresWidget.class));
        final int N = appWidgetIds.length;

        Uri dateUri = DatabaseContract.scores_table.buildScoreWithDate();
        Date today = new Date(System.currentTimeMillis());
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
        Cursor cursor = getContentResolver().query(dateUri, columns, "date", new String[] { mformat.format(today) }, null);

        if (cursor == null) {
            for (int i = 0; i < N; i++) {
                updateWidgetNoData(appWidgetManager, appWidgetIds[i]);
            }
            return;
        }

        if (cursor.moveToFirst() == false) {
            cursor.close();
            for (int i = 0; i < N; i++) {
                updateWidgetNoData(appWidgetManager, appWidgetIds[i]);
            }
            return;
        }

        String home_team = cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.HOME_COL));
        String away_team = cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.AWAY_COL));
        int home_goals = cursor.getInt(cursor.getColumnIndex(DatabaseContract.scores_table.HOME_GOALS_COL));
        int away_goals = cursor.getInt(cursor.getColumnIndex(DatabaseContract.scores_table.AWAY_GOALS_COL));
        String league = cursor.getString(cursor.getColumnIndex(DatabaseContract.scores_table.LEAGUE_COL));
        cursor.close();

        for (int i = 0; i < N; i++) {
            updateWidgetWithScore(home_goals, away_goals, home_team, away_team, appWidgetManager, appWidgetIds[i]);
        }
    }

    private void updateWidgetWithScore(int home_goals, int away_goals, String home_team, String away_team, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.scores_widget);
        if (home_goals >= 0 && away_goals >= 0)
            views.setTextViewText(R.id.widget_score, String.valueOf(home_goals) + " - " + String.valueOf(away_goals));
        else
            views.setTextViewText(R.id.widget_score, "-");
        views.setTextViewText(R.id.widget_home_team, home_team);
        views.setTextViewText(R.id.widget_away_team, away_team);
        Intent mainActivity = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, mainActivity, 0);
        views.setOnClickPendingIntent(R.id.appwidget, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private void updateWidgetNoData(AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.scores_widget);
        views.setTextViewText(R.id.widget_score, getString(R.string.no_match));
        views.setTextViewText(R.id.widget_home_team, "");
        views.setTextViewText(R.id.widget_away_team, "");
        Intent mainActivity = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, mainActivity, 0);
        views.setOnClickPendingIntent(R.id.appwidget, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
