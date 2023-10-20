/*
 * Copyright (c) 2021 Nico Kuijpers
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

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class BodySelectorPanel extends Stage {

    // Definition of constant values
    private static final int PANELWIDTH = 480;
    private static final int PANELHEIGHT = 315;
    private static final int CHECKBOXWIDTH = 110;
    private static final int GAPSIZE = 10;
    private static final int BORDERSIZE = 10;

    // Grid pane
    private final GridPane grid;

    // Font for labels of check boxes
    private Font font;

    // Size for check boxes within grid pane
    private final int horsize = 1;
    private final int versize = 1;

    // Location of next checkbox within grid pane
    private int rowIndex = 0;
    private int colIndex = 0;


    /**
     * Constructor.
     */
    public BodySelectorPanel() {

        // Define grid pane
        grid = new GridPane();
        grid.setHgap(GAPSIZE);
        grid.setVgap(GAPSIZE);
        grid.setPadding(new Insets(BORDERSIZE, BORDERSIZE, BORDERSIZE, BORDERSIZE));

        // For debug purposes
        // Make the grid lines visible
        // grid.setGridLinesVisible(true);

        // Define font for labels of check boxes
        font = new Font("Arial",13);

        // Create the scene and add the grid pane
        Group root = new Group();
        Scene scene = new Scene(root,PANELWIDTH,PANELHEIGHT);
        root.getChildren().add(grid);
        this.setTitle("Solar System Bodies");
        this.setScene(scene);
    }

    /**
     * Create a check box with given label and tool tip and place the new
     * check box in this panel.
     * @param label        Text to be placed on the label of the check box
     * @param toolTipText  Text to be placed in the tool tip of the check box
     * @return the new instance of CheckBox
     */
    public CheckBox createCheckBox(String label, String toolTipText) {
        CheckBox checkBox = new CheckBox(label);
        checkBox.setMinWidth(CHECKBOXWIDTH);
        checkBox.setMaxWidth(CHECKBOXWIDTH);
        checkBox.setFont(font);
        Tooltip toolTip = new Tooltip(toolTipText);
        checkBox.setTooltip(toolTip);
        grid.add(checkBox,colIndex,rowIndex,horsize,versize);
        rowIndex++;
        if (rowIndex >= 11) {
            rowIndex = 0;
            colIndex++;
        }
        return checkBox;
    }
}
