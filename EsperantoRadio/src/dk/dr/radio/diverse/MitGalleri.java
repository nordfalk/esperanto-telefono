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
package dk.dr.radio.diverse;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Gallery;
import eo.radio.datumoj.Log;

/**
 * http://stackoverflow.com/questions/4311854/how-can-i-limit-fling-in-android-gallery-to-just-one-item-per-fling
 * @author j
 */
public class MitGalleri extends Gallery {
  public MitGalleri(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public MitGalleri(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public MitGalleri(Context context) {
    super(context);
  }
  //  1500.0f
  public float MAKS_RAPIDECO = 3 * this.getContext().getResources().getDisplayMetrics().widthPixels;
  /*
   {
   Log.d("MAKS_RAPIDECO="+MAKS_RAPIDECO);
   Log.d("MAKS_RAPIDECO="+MAKS_RAPIDECO);
   Log.d("MAKS_RAPIDECO="+MAKS_RAPIDECO);
   Log.d("MAKS_RAPIDECO="+MAKS_RAPIDECO);
   }
   */

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    //limit the max speed in either direction
    if (velocityX > MAKS_RAPIDECO) {
      velocityX = MAKS_RAPIDECO;
    } else if (velocityX < -MAKS_RAPIDECO) {
      velocityX = -MAKS_RAPIDECO;
    }

    return super.onFling(e1, e2, velocityX, velocityY);
  }
}
