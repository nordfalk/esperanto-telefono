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
package dk.nordfalk.esperanto.radio.datumoj;

import java.util.Date;

/**
 *
 * @author j
 */
public class Elsendo {
	public String kanalNomo;
	public String datoStr;
	public Date dato;
	public String sonoUrl;
	public String rektaElsendaPriskriboUrl;
	public String priskribo;
	public String titolo;
	public String ligilo;
  public String emblemoUrl;
	/** Por Muzaiko la elsenda galerio malaperu se oni elektas la rektan elsendon */
	public boolean elektoIgasLaGalerioMalaperi;

	public String toString() {
		return kanalNomo+dato+(priskribo.length()>30?priskribo.substring(0,15)+"...":priskribo);
	}
}
