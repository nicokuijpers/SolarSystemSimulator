/*
 * Copyright (c) 2019 Nico Kuijpers
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR I
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package application;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

/**
 * A DateTimePicker with configurable datetime format where both date and time
 * can be changed via the text field and the date can additionally be changed
 * via the JavaFX default date picker.
 * Note by Nico Kuijpers: the source code for this class is obtained from GitHub.
 * https://github.com/edvin/tornadofx-controls/blob/master/src/main/java/tornadofx/control/DateTimePicker.java
 */
@SuppressWarnings("unused")
public class DateTimePicker extends DatePicker {

    public static final String DefaultFormat = "yyyy-MM-dd HH:mm";
    
    private DateTimeFormatter formatter;
    private ObjectProperty<LocalDateTime> dateTimeValue = new SimpleObjectProperty<>(LocalDateTime.now());
    private ObjectProperty<String> format = new SimpleObjectProperty<String>() {
        public void set(String newValue) {
            super.set(newValue);
            formatter = DateTimeFormatter.ofPattern(newValue);
        }
    };
    
    // Code added by Nico Kuijpers to support AD/BC
    // era = 0 means Before Christ (BC)
    // era = 1 means Anno Domini (AD)
    private int era = 1;
    
    public int getEra() {
        return era;
    }

    public void alignColumnCountWithFormat() {
        getEditor().setPrefColumnCount(getFormat().length());
    }

    public DateTimePicker() {
        getStyleClass().add("datetime-picker");
        setFormat(DefaultFormat);
        setConverter(new InternalConverter());
        alignColumnCountWithFormat();

        // Synchronize changes to the underlying date value back to the dateTimeValue
        valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                dateTimeValue.set(null);
            } else {
                if (dateTimeValue.get() == null) {
                    dateTimeValue.set(LocalDateTime.of(newValue, LocalTime.now()));
                } else {
                    LocalTime time = dateTimeValue.get().toLocalTime();
                    dateTimeValue.set(LocalDateTime.of(newValue, time));
                }
            }
        });

        // Synchronize changes to dateTimeValue back to the underlying date value
        dateTimeValue.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                LocalDate dateValue = newValue.toLocalDate();
                boolean forceUpdate = dateValue.equals(valueProperty().get());
                
                // Make sure the display is updated even when the date itself wasn't changed
                setValue(dateValue);
                if (forceUpdate) {
                    setConverter(new InternalConverter());
                }

            } else {
                setValue(null);
            }
        });

        // Persist changes onblur
        getEditor().focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                simulateEnterPressed();
            }
        });
    }

    private void simulateEnterPressed() {
        getEditor().commitValue();
    }

    public LocalDateTime getDateTimeValue() {
        return dateTimeValue.get();
    }

    public void setDateTimeValue(LocalDateTime dateTimeValue) {
        this.dateTimeValue.set(dateTimeValue);
    }

    public ObjectProperty<LocalDateTime> dateTimeValueProperty() {
        return dateTimeValue;
    }

    public String getFormat() {
        return format.get();
    }

    public ObjectProperty<String> formatProperty() {
        return format;
    }

    public void setFormat(String format) {
        this.format.set(format);
        alignColumnCountWithFormat();
    }

    class InternalConverter extends StringConverter<LocalDate> {

        @Override
        public String toString(LocalDate object) {
            LocalDateTime value = getDateTimeValue();
            // Start: code added by Nico Kuijpers to support BC/AD
            if (value != null && format.get().startsWith("G")) {
                int era = value.get(ChronoField.ERA);
                String result = value.format(formatter);
                if (era == 0) {
                    // Before Christ (BC)
                    if (result.startsWith("AD")) {
                        return result.replace("AD", "BC");
                    }
                }
                else {
                    // Anno Domini (AD)
                    if (result.startsWith("BC")) {
                        return result.replace("BC", "AD");
                    }
                }
            }
            // End: code added by Nico Kuijpers to support BC/AD
            return (value != null) ? value.format(formatter) : "";
        }

        @Override
        public LocalDate fromString(String value) {
            if (value == null || value.isEmpty()) {
                dateTimeValue.set(null);
                return null;
            }
            
            // Start: code added by Nico Kuijpers to support BC/AD
            if (format.get().startsWith("G")) {
                if (value.startsWith("BC")) {
                    // Before Christ (BC)
                    era = 0;
                }
                else if (value.startsWith("AD")) {
                    // Anno Domini (AD)
                    era = 1;
                }   
            }
            // End: code added by Nico Kuijpers to suppport BC/AD
            
            dateTimeValue.set(LocalDateTime.parse(value, formatter));
            return dateTimeValue.get().toLocalDate();
        }
    }
}
