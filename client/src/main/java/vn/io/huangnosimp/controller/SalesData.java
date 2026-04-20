package vn.io.huangnosimp.controller;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

public class SalesData {
    public static ObservableList<XYChart.Series<Number, Number>> getSalesData(){
        XYChart.Series<Number, Number> productOne = new XYChart.Series<>();
        productOne.setName("Product One");
        productOne.getData().addAll(new XYChart.Data<>(2020, 500));
        productOne.getData().addAll(new XYChart.Data<>(2021, 4000));
        productOne.getData().addAll(new XYChart.Data<>(2022, 3000));
        productOne.getData().addAll(new XYChart.Data<>(2023, 200));
        productOne.getData().addAll(new XYChart.Data<>(2024, 100));
        productOne.getData().addAll(new XYChart.Data<>(2025, 900));
        productOne.getData().addAll(new XYChart.Data<>(2026, 2000));

        XYChart.Series<Number, Number> productTwo = new XYChart.Series<>();
        productTwo.setName("Product Two");
        productTwo.getData().addAll(new XYChart.Data<>(2020, 500));
        productTwo.getData().addAll(new XYChart.Data<>(2021, 1000));
        productTwo.getData().addAll(new XYChart.Data<>(2022, 600));
        productTwo.getData().addAll(new XYChart.Data<>(2023, 800));
        productTwo.getData().addAll(new XYChart.Data<>(2024, 2000));
        productTwo.getData().addAll(new XYChart.Data<>(2025, 900));
        productTwo.getData().addAll(new XYChart.Data<>(2026, 1500));

        ObservableList<XYChart.Series<Number, Number>> data = FXCollections.observableArrayList();
        data.addAll(productOne, productTwo);
        return data;
    }
}
