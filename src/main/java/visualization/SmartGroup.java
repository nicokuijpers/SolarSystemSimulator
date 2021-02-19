package visualization;

import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

/**
 * @author afsal villan
 * @version 1.0
 *
 * http://www.genuinecoder.com
 */
class SmartGroup extends Group {

    Rotate r;
    Transform t = new Rotate();

    void rotateByX(int ang) {
        r = new Rotate(ang, Rotate.X_AXIS);
        t = t.createConcatenation(r);
        this.getTransforms().clear();
        this.getTransforms().addAll(t);
    }

    void rotateByY(int ang) {
        r = new Rotate(ang, Rotate.Y_AXIS);
        t = t.createConcatenation(r);
        this.getTransforms().clear();
        this.getTransforms().addAll(t);
    }
}
