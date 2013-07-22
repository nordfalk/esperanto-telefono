/**
Esperanto-radio por Androjd, farita de Jacob Nordfalk.
Kelkaj partoj de la kodo originas de DR Radio 2 por Android, vidu
http://code.google.com/p/dr-radio-android/

Esperanto-radio por Androjd estas libera softvaro: vi povas redistribui
ĝin kaj/aŭ modifi ĝin kiel oni anoncas en la licenco GNU Ĝenerala Publika
Licenco (GPL) versio 2.

Esperanto-radio por Androjd estas distribuita en la espero ke ĝi estos utila,
sed SEN AJNA GARANTIO; sen eĉ la implica garantio de surmerkatigindeco aŭ
taŭgeco por iu aparta celo.
Vidu la GNU Ĝenerala Publika Licenco por pli da detaloj.

Vi devus ricevi kopion de la GNU Ĝenerala Publika Licenco kune kun la
programo. Se ne, vidu <http://www.gnu.org/licenses/>.
 */

package dk.dr.radio.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;

/**
 * Da setContentDescription() tilsyneladende bliver ignoreret på ImageViews
 * er vi nødt til at omdefinere dispatchPopulateAccessibilityEvent
 * for at få tekst læst højt.
 * TODO: Se om vi kan erstatte ImageView med en ImageButton i kanalvalgs
 * layout så vi kan droppe klassen her igen
 * @author j
 */
public class ImageViewTilBlinde extends ImageView {
    public ImageViewTilBlinde(Context context) {
        super(context);
    }

    public ImageViewTilBlinde(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageViewTilBlinde(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public String blindetekst;

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (blindetekst==null) return false;
        event.getText().add(blindetekst);
        return true;
    }

}
