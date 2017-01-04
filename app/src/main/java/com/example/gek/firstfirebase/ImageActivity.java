package com.example.gek.firstfirebase;

import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ImageActivity extends AppCompatActivity {

    private static final int REQUEST_LOAD_IMG = 1;
    public static final String MY_STORAGE = "gs://firstfirebase-71d41.appspot.com";
    public static final String MY_FOLDER = "images";

    ImageView ivImage;
    Button btnSend;
    TextView tvStatus;

    private Uri uriImage;



    public static final String TAG = "GEK";

    private StorageReference storageRef;
    private FirebaseStorage storage;
    private StorageReference folderRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        ivImage = (ImageView)findViewById(R.id.ivImage);
        tvStatus = (TextView)findViewById(R.id.tvStatus);


        // create an instance of FirebaseStorage:
        storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        storageRef = storage.getReferenceFromUrl(MY_STORAGE);
        // Создаем ссылку на папку в хранилище
        folderRef = storageRef.child(MY_FOLDER);
        Log.d(TAG, "onCreate: Path of folderRef =  " + folderRef.getPath());


        findViewById(R.id.btnChoose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        btnSend = (Button)findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToServer(uriImage);
            }
        });
    }



    private void chooseImage(){
        tvStatus.setText("Интент на выбор файла с галереи");
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        if (photoPickerIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(photoPickerIntent, REQUEST_LOAD_IMG);
        }
    }

    // Обработка вызванных ранее интентов:
    //   - запрос картинки из галереи возвращает URI
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null ) {
            if (requestCode == REQUEST_LOAD_IMG ) {
                Uri uri = data.getData();
                ivImage.setImageURI(uri);
                uriImage = uri;
                btnSend.setEnabled(true);
                tvStatus.append("\nВыбран файл: \n URI = " + uri.toString());
            }
        }
    }

    private void sendToServer(Uri uriImage){
        //todo установить нормальное имя для файла, а не 4 цифры с ури устройства, которые будут не уникальные
        //логичнее всего использоватьза основу мыло, что уже уникально или время (или вместе)
        StorageReference currentImageRef = folderRef.child(uriImage.getLastPathSegment() + ".jpg");
        final Long ms = SystemClock.uptimeMillis();
        tvStatus.append("\nНачало загрузки на сервер ");
        UploadTask uploadTask = currentImageRef.putFile(uriImage);


        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();

                long time = (SystemClock.uptimeMillis() - ms);
                tvStatus.append("\nЗагружен на сервер за " + time + "ms");
                tvStatus.append("\nСсылка на файл " + downloadUrl);
                Log.d(TAG, "onSuccess: " + downloadUrl);

            }
        });
    }

}
