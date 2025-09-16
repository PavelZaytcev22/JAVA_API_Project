package com.example.smartprinter.ui.printer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PrinterViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public PrinterViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Нет подключенных устройств");
    }

    public LiveData<String> getText() {
        return mText;
    }
}