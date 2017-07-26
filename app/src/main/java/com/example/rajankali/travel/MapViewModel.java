package com.example.rajankali.travel;

import android.databinding.ObservableField;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by rajan.kali on 7/26/2017.
 * View Binding - Map View
 */

public class MapViewModel {

    private MapInteractor mapInteractor;
    private Timer timer;
    private int seconds = 0;
    private LeakProofHandler leakProofHandler;
    public ObservableField<String> distanceCovered = new ObservableField<>();
    public ObservableField<String> travelTime = new ObservableField<>();
    public ObservableField<String> buttonText = new ObservableField<>();


    MapViewModel(MapInteractor mapInteractor) {
        this.mapInteractor = mapInteractor;
        leakProofHandler = new LeakProofHandler(this);
        reset();
    }

    private void reset() {
        distanceCovered.set("Distance\n0 kms");
        travelTime.set("Time Travelled\n00:00:00");
        buttonText.set("start");
    }

    void onDistanceChanged(double distanceInKilometers) {
        distanceCovered.set("Distance\n" + distanceInKilometers + " km");
    }

    public void onClick() {
        if (buttonText.get().equals("start")) {
            buttonText.set("stop");
            mapInteractor.showToast("Ride Started");
            distanceCovered.set("Distance\n0 kms");
            travelTime.set("Time Travelled\n00:00:00");
            mapInteractor.onRideStart();
            timer = new Timer();
            TimerTask timerTask = new myTimerTask();
            timer.schedule(timerTask, 0, 1000);
        } else { //stop clicked
            buttonText.set("start");
            timer.cancel();
            mapInteractor.showToast("Ride Ended");
            mapInteractor.onRideEnd();
        }
    }

    private class myTimerTask extends TimerTask {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            seconds++;
            leakProofHandler.sendEmptyMessage(seconds);
        }
    }

    private static class LeakProofHandler extends Handler {
        private final WeakReference<MapViewModel> mapModel;

        LeakProofHandler(MapViewModel model) {
            mapModel = new WeakReference<>(model);
        }

        @Override
        public void handleMessage(Message msg) {
            int seconds = msg.what % 60;
            int minutes = (msg.what / 60) % 60;
            int hours = (msg.what / 3600);
            mapModel.get().travelTime.set("Travel Time\n" + String.format(Locale.ENGLISH, "%02d : %02d : %02d", hours, minutes, seconds));
        }
    }
}
