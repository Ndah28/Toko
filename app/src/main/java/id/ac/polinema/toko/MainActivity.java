package id.ac.polinema.toko;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collections;

import id.ac.polinema.toko.models.data_toko;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //Deklarasi Variable
    private ProgressBar progressBar;
    private EditText Kode, Nama, Harga;
    private FirebaseAuth auth;
    private Button Logout, Simpan, Login, ShowData;

    //Membuat Kode Permintaan
    private int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progress);
        progressBar.setVisibility(View.GONE);

        //Inisialisasi ID (Button)
        Logout = findViewById(R.id.logout);
        Logout.setOnClickListener(this);
        Simpan = findViewById(R.id.save);
        Simpan.setOnClickListener(this);
        Login = findViewById(R.id.login);
        Login.setOnClickListener(this);
        ShowData = findViewById(R.id.showdata);
        ShowData.setOnClickListener(this);

        auth = FirebaseAuth.getInstance(); //Mendapakan Instance Firebase Autentifikasi

        //Inisialisasi ID (EditText)
        Kode = findViewById(R.id.kode);
        Nama = findViewById(R.id.nama);
        Harga = findViewById(R.id.harga);

        /*
         * Mendeteksi apakah ada user yang masuk, Jika tidak, maka setiap komponen UI akan dinonaktifkan
         * Kecuali Tombol Login. Dan jika ada user yang terautentikasi, semua fungsi/komponen
         * didalam User Interface dapat digunakan, kecuali tombol Logout
         */
        if(auth.getCurrentUser() == null){
            defaultUI();
        }else {
            updateUI();
        }
    }

    //Tampilan Default pada Activity jika user belum terautentikasi
    private void defaultUI(){
        Logout.setEnabled(false);
        Simpan.setEnabled(false);
        ShowData.setEnabled(false);
        Login.setEnabled(true);
        Kode.setEnabled(false);
        Nama.setEnabled(false);
        Harga.setEnabled(false);
    }

    //Tampilan User Interface pada Activity setelah user Terautentikasi
    private void updateUI(){
        Logout.setEnabled(true);
        Simpan.setEnabled(true);
        Login.setEnabled(false);
        ShowData.setEnabled(true);
        Kode.setEnabled(true);
        Nama.setEnabled(true);
        Harga.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    // Mengecek apakah ada data yang kosong
    private boolean isEmpty(String s){
        return TextUtils.isEmpty(s);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN adalah kode permintaan yang Anda berikan ke startActivityForResult, saat memulai masuknya arus.
        if (requestCode == RC_SIGN_IN) {

            //Berhasil masuk
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "Login Berhasil", Toast.LENGTH_SHORT).show();
                updateUI();
            }else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Login Dibatalkan", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login:
                // Statement program untuk login/masuk
                startActivityForResult(AuthUI.getInstance()
                                .createSignInIntentBuilder()

                                //Memilih Provider atau Method masuk yang akan kita gunakan
                                .setAvailableProviders(Collections.singletonList(new AuthUI.IdpConfig.GoogleBuilder().build()))
                                .setIsSmartLockEnabled(false)
                                .build(),
                        RC_SIGN_IN);
                progressBar.setVisibility(View.VISIBLE);
                break;

            case R.id.save:
                //Mendapatkan UserID dari pengguna yang Terautentikasi
                String getUserID = auth.getCurrentUser().getUid();

                //Mendapatkan Instance dari Database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference getReference;

                //Menyimpan Data yang diinputkan User kedalam Variable
                String getKode = Kode.getText().toString();
                String getNama = Nama.getText().toString();
                String getHarga = Harga.getText().toString();

                getReference = database.getReference(); // Mendapatkan Referensi dari Database

                // Mengecek apakah ada data yang kosong
                if(isEmpty(getKode) || isEmpty(getNama) || isEmpty(getHarga)){
                    //Jika Ada, maka akan menampilkan pesan singkan seperti berikut ini.
                    Toast.makeText(MainActivity.this, "Data tidak boleh ada yang kosong", Toast.LENGTH_SHORT).show();
                }else {
                    /*
                    Jika Tidak, maka data dapat diproses dan meyimpannya pada Database
                    Menyimpan data referensi pada Database berdasarkan User ID dari masing-masing Akun
                    */
                    getReference.child("Admin").child(getUserID).child("Customer").push()
                            .setValue(new data_toko(getKode,getNama,getHarga))
                            .addOnSuccessListener(this, new OnSuccessListener() {
                                @Override
                                public void onSuccess(Object o) {
                                    //Peristiwa ini terjadi saat user berhasil menyimpan datanya kedalam Database
                                    Kode.setText("");
                                    Nama.setText("");
                                    Harga.setText("");
                                    Toast.makeText(MainActivity.this, "Data Tersimpan", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                break;

            case R.id.logout:
                // Statement program untuk logout/keluar
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                Toast.makeText(MainActivity.this, "Logout Berhasil", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                break;

            case R.id.showdata:
                startActivity(new Intent(MainActivity.this, MyListData.class));
                break;
        }
    }
}
