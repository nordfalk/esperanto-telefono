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
package eo.radio.datumoj;

import java.util.ArrayList;
import org.json.JSONObject;

public class Kanalo {
  public String kodo;
  public String nomo;
  //public String rektaElsendaSonoUrl;
  //public String rektaElsendaPriskriboUrl;
  //public Bitmap emblemo;
  public String hejmpaĝoEkrane;
  public String hejmpaĝoButono;
  public String retpoŝto;
  public JSONObject json;
  public ArrayList<Elsendo> elsendoj = new ArrayList<Elsendo>();
  public Elsendo rektaElsendo;
  public String emblemoUrl;

  @Override
  public String toString() {
    return kodo + "/" + nomo + "/" + elsendoj.size() + "\n";
  }
}
