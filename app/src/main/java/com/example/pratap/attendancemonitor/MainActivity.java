package com.example.pratap.attendancemonitor;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.pratap.attendancemonitor.data.SubjectContract.SubjectEntry;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the subject data loader */
    private static final int SUBJECT_LOADER = 0;

    /** Adapter for the ListView */
    MonitorCursorAdapter mCursorAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content of the activity to use the activity_main.xml layout file
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SubjectActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the subject data
        ListView subjectListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        subjectListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of subject data in the Cursor.
        // There is no subject data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new MonitorCursorAdapter(this, null);
        subjectListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        subjectListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link SubjectActivity}
                Intent intent = new Intent(MainActivity.this, SubjectActivity.class);

                // Form the content URI that represents the specific pet that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link SubjectEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.pratap.attendancemonitor/subjects/2"
                // if the pet with ID 2 was clicked on.
                Uri currentSubjectUri = ContentUris.withAppendedId(SubjectEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentSubjectUri);

                // Launch the {@link EditorActivity} to display the data for the current pet.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(SUBJECT_LOADER, null, this);
    }

    /**
     * Helper method to delete all pets in the database.
     */
    private void deleteAllSubjects() {
        int rowsDeleted = getContentResolver().delete(SubjectEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from monitor database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_main.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllSubjects();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                SubjectEntry._ID,
                SubjectEntry.COLUMN_SUBJECT_NAME,
                SubjectEntry.COLUMN_NUMBER_OF_DAYS_PRESENT,
                SubjectEntry.COLUMN_NUMBER_OF_DAYS_ABSENT };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                SubjectEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link MonitorCursorAdapter} with this new cursor containing updated subject data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}