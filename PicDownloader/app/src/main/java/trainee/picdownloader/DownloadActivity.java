package trainee.picdownloader;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.AsyncTaskLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import android.widget.Toast;

public class DownloadActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks <Object> {
    private final int loader = 0;
    private String dir = Environment.getExternalStorageDirectory().getPath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        findViewById(R.id.progressBar).setVisibility(ProgressBar.INVISIBLE);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 if (getSupportLoaderManager().getLoader(loader) != null) {
                     Intent intent = new Intent();
                     intent.setAction(Intent.ACTION_VIEW);
                     intent.setDataAndType(Uri.parse("file://" + "/storage/emulated/0/testimage.jpg"), "image/*");
                     List<ResolveInfo> allHandlers = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                     String name = allHandlers.get(0).activityInfo.name;
                     String pack = allHandlers.get(0).activityInfo.packageName;
                     intent.setClassName(pack, name);
                     startActivity(intent);
                 } else {
                     findViewById(R.id.button).setEnabled(false);
                     getSupportLoaderManager().initLoader(loader, null, DownloadActivity.this);
                     getSupportLoaderManager().getLoader(loader).forceLoad();
                     findViewById(R.id.progressBar).setVisibility(ProgressBar.VISIBLE);
                 }
            }
        });
        ImageView img = (ImageView) findViewById(R.id.imageView);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        ((TextView) this.findViewById(R.id.statusLabel)).setText(getResources().getString(R.string.downloaded));
        ((Button) this.findViewById(R.id.button)).setText(getResources().getString(R.string.open));
        findViewById(R.id.button).setEnabled(true);
        ((ImageView)this.findViewById(R.id.imageView)).setImageDrawable(Drawable.createFromPath(Environment.getExternalStorageDirectory().getPath() + "/" + "testimage.jpg"));

    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new Loader(DownloadActivity.this) {
            public Object start() {
                return new AsyncTaskLoader(DownloadActivity.this) {
                    @Override
                    public Object loadInBackground() {
                        try {
                            URL url = new URL(getResources().getString(R.string.URL2));
                            URLConnection connection = url.openConnection();
                            connection.connect();

                            int fileLength = connection.getContentLength();
                            String filePath = Environment.getExternalStorageDirectory().getPath();

                            InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                            OutputStream outputStream = new FileOutputStream(filePath + "/" + "testimage.jpg");

                            byte data[] = new byte[fileLength > 0 ? fileLength / 100 : 1024];
                            long total = 0;
                            int count;
                            while ((count = inputStream.read(data)) != -1) {
                                total += count;
                                ((ProgressBar) findViewById(R.id.progressBar)).setProgress((int) (total * 100 / fileLength));
                                outputStream.write(data, 0, count);
                            }
                            outputStream.flush();
                            outputStream.close();
                            inputStream.close();
                        } catch (Exception e) {
                            Log.e("Sheat", " happens");
                            e.printStackTrace();
                            Toast.makeText(DownloadActivity.this, "Sheat", Toast.LENGTH_SHORT).show();
                        }
                        return null;
                    }
                };
            }
        };
    }


    @Override
    public void onLoaderReset(Loader loader) {
        Toast.makeText(this, "onLoaderReset", Toast.LENGTH_SHORT).show();
    }
}
