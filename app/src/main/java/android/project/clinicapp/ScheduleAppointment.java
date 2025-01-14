package android.project.clinicapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.project.clinicapp.API.ClinicAPI;
import android.project.clinicapp.models.Fecha;
import android.project.clinicapp.models.FechaResultado;
import android.project.clinicapp.models.Hora;
import android.project.clinicapp.models.HorasResultado;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ScheduleAppointment extends AppCompatActivity {

    private Spinner spin1;
    private ImageView back;
    private ChipGroup especialidadesChip, fechasChip, horasChip;
    private Chip chip;
    Button btnRegistrar;
    String especialidad, fecha, hora;

    private Retrofit retrofit;

    private static final String TAG = "CITA A PROGRAMAR";
    public static final String BASE_URL = "https://clinicauniversitaria.herokuapp.com/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_appointment);

        spin1 = (Spinner)findViewById(R.id.spinnerFecha);
        String [] opc = {"Enero, 2022","Febrero, 2022","Marzo, 2022","Abril, 2022","Mayo, 2022","Junio, 2022",
                "Julio, 2022","Agosto, 2022","Septiembre, 2022","Octubre, 2022","Noviembre, 2022","Diciembre, 2022",
                "Enero, 2023","Febrero, 2023","Marzo, 2023","Abril, 2023","Mayo, 2023","Junio, 2023","Julio, 2023",
                "Agosto, 2023","Septiembre, 2023","Octubre, 2023","Noviembre, 2023","Diciembre, 2023"};
        ArrayAdapter<String> adap = new ArrayAdapter<String>(this, R.layout.spinner_atributo,opc);
        spin1.setAdapter(adap);

        // Inicializamos Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder().serializeNulls().create()
                ))
                .build();

        //List<Integer> ids = especialidadChip.getCheckedChipIds();
        //int ids = especialidadChip.getCheckedChipId();
        //especialidadChip.setOnCheckedChangeListener(ChipGroup.OnCheckedChangeListener listener)

        // ESPECIALIDAD
        especialidadesChip = (ChipGroup) findViewById(R.id.especialidadesChipGroup);
        especialidadesChip.setOnCheckedChangeListener((group, checkedId) -> {
            chip = (Chip) especialidadesChip.findViewById(checkedId);
            Log.i(TAG,"ID"+checkedId );
            especialidad = chip.getText().toString();
            solicitarDataFechas(especialidad);
        });


        // FECHAS
        fechasChip = (ChipGroup) findViewById(R.id.fechasChipGroup);
        fechasChip.setOnCheckedChangeListener((group, checkedId) -> {
            chip = (Chip) fechasChip.findViewById(checkedId);
            Log.i("FECHA", "ID: "+ checkedId);
            fecha = "2022-01-"+chip.getText().subSequence(0, 2).toString();
            solicitarDataHoras(especialidad, fecha);
            // 21 LUN -> "2022-01-"+${};
        });

        // HORAS
        horasChip = (ChipGroup) findViewById(R.id.horasChipGroup);
        horasChip.setOnCheckedChangeListener((group, checkedId) -> {
            chip = (Chip) horasChip.findViewById(checkedId);
            Log.i("ID", "ID: "+checkedId);
            hora = chip.getText().subSequence(0,5).toString()+":00";
            //02:00:00
        });

        // Agregamos evento de click para guardar la cita
        btnRegistrar = (Button) findViewById(R.id.btnRegistrarCita);
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Logica de validación de campos seleccionados
                Log.i("CITA PROG", "Especialidad: "+especialidad);
                Log.i("CITA PROG", "Fecha: "+fecha);
                Log.i("CITA PROG", "Hora: "+hora);

                // Llamamos al método para enviar los datos a la API
                //sendAppointmentToAPI("Médicina", "2022-01-30", "18:00:00", 3);
                //sendAppointmentToAPI(especialidad, fecha, hora, 1);

                Intent inte = new Intent(view.getContext(), CardPayment.class);
                // Envía los datos de la cita a la interfaz de pago
                inte.putExtra("especialidad", especialidad);
                inte.putExtra("fecha", fecha);
                inte.putExtra("hora", hora);
                startActivity(inte);
                //Toast.makeText(ScheduleAppointment.this, "Su cita ha sido programada", Toast.LENGTH_SHORT).show();
            }
        });

        back = (ImageView) findViewById(R.id.imgBackProg);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inte = new Intent(v.getContext(), Menu.class);
                startActivity(inte);
            }
        });
    }

    public void solicitarDataFechas(String especialidad) {
        ClinicAPI service = retrofit.create(ClinicAPI.class);
        Call<FechaResultado> citaResultadoCall = service.filterDate(especialidad);
        //ArrayList<Fecha> listaFechas;

        citaResultadoCall.enqueue(new Callback<FechaResultado>() {
            @Override
            public void onResponse(Call<FechaResultado> call, Response<FechaResultado> response) {
                if (response.isSuccessful()){
                    FechaResultado Respuesta = response.body();
                    // Se pasa todos los resultados a la lista de Ricks
                    ArrayList<Fecha> listaFechas = Respuesta.getResults();
                    habilitarFechas(listaFechas);
                    for (int i=0; i<listaFechas.size(); i++) {
                        //chip = (Chip) fechasChip.findViewById(i);
                        //Log.e(TAG, " onFailure: "+ t.getMessage());
                        Fecha p = listaFechas.get(i);
                        Log.i(TAG, "Número de día: "+ p.getDia());
                        Log.i(TAG, "Disponible: "+ p.getDisponible());
                    }
                }else{
                    Log.e(TAG, " onResponse: "+response.errorBody());
                }
            }
            @Override
            public void onFailure(Call<FechaResultado> call, Throwable t) {
                Log.e(TAG, " onFailure: "+ t.getMessage());
            }
        });
    }

    public void habilitarFechas(ArrayList<Fecha> listaFechas){
        int ids[] = {2131362288, 2131362296, 2131362324, 2131362267, 2131362650, 2131362435, 2131362287};
        for (int i=0; i<listaFechas.size(); i++){
            chip = (Chip) fechasChip.findViewById(ids[i]);
            Fecha f = listaFechas.get(i);
            if(!f.getDisponible()){
                chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.disabled)));
            }
        }
    }

    public void solicitarDataHoras(String especialidad, String fecha) {
        ClinicAPI service = retrofit.create(ClinicAPI.class);
        Call<HorasResultado> citaResultadoCall = service.filterTimes(especialidad, fecha);

        citaResultadoCall.enqueue(new Callback<HorasResultado>() {
            @Override
            public void onResponse(Call<HorasResultado> call, Response<HorasResultado> response) {
                if (response.isSuccessful()) {
                    HorasResultado Respuesta = response.body();
                    // Se pasa todos los resultados a la lista de Ricks
                    ArrayList<Hora> listaHoras = Respuesta.getResults();
                    habilitarHoras(listaHoras);
                    for (int i = 0; i < listaHoras.size(); i++) {
                        //chip = (Chip) horasChip.findViewById(i);
                        Hora p = listaHoras.get(i);
                        Log.i(TAG, "Número de hora: " + p.getHora());
                        Log.i(TAG, "Disponible: " + p.getDisponible());
                    }
                } else {
                    Log.e(TAG, " onResponse: " + response.errorBody());
                }
            }
            @Override
            public void onFailure(Call<HorasResultado> call, Throwable t) {
                Log.e(TAG, " onFailure: " + t.getMessage());
            }
        });
    }

    public void habilitarHoras(ArrayList<Hora> listaHoras){
        int ids[] = {2131362001, 2131362002, 2131362003, 2131362004, 2131362005, 2131362006, 2131362007, 2131362008, 2131362009, 2131362011, 2131362012, 2131362013, 2131362014, 2131362015, 2131362016, 2131362017, 2131362018, 2131362019, 2131362020, 2131362022, 2131362023};
        /*for (int i=0; i<listaHoras.size(); i++){
            chip = (Chip) horasChip.findViewById(ids[i]);
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.design_default_color_background)));
        }*/
        for (int i=0; i<listaHoras.size(); i++){
            chip = (Chip) horasChip.findViewById(ids[i]);
            Hora h = listaHoras.get(i);
            if(!h.getDisponible()){
                chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this,R.color.disabled)));
            }
        }
    }
}