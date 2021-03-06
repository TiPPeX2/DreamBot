package nezz.dreambot.scriptmain.hillprayer;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.utilities.impl.Condition;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;

import java.awt.*;

@ScriptManifest(name = "Prayer on the Hill", author = "Nezz", description = "Kills hill giants and buries their bones", version = 1, category = Category.PRAYER)
public class HillPrayer extends AbstractScript {

	private Timer t = new Timer();

	private enum State{
		KILL, LOOT, BURY, SLEEP
	}
	
	private State getState(){
		if(getLocalPlayer().isInCombat()){
			return State.SLEEP;
		}
		else{
			GroundItem gi = getGroundItems().closest("Big bones", "Limpwurt root");
			if(gi != null){
				return State.LOOT;
			}
			else if(getInventory().contains("Big bones")){
				return State.BURY;
			}
			else
				return State.KILL;
		}
	}
	
	private State state = null;
	
	@Override
	public void onStart() {
		getSkillTracker().start(Skill.PRAYER);
	}

	@Override
	public int onLoop() {
		if (!getClient().isLoggedIn()) {
			return 600;
		}
		state = getState();
		switch(state){
		case BURY:
			getInventory().interact("Big bones", "Bury");
			sleep(600,900);
			break;
		case KILL:
			if(getLocalPlayer().isInCombat()){
				return Calculations.random(300,600);
			}
			NPC giant = getNpcs().closest(new Filter<NPC>(){
				public boolean match(NPC n){
					if(n == null || n.getName() == null || !n.getName().equals("Hill Giant"))
						return false;
					if(n.isInCombat() && (n.getInteractingCharacter() == null || !n.getInteractingCharacter().getName().equals(getLocalPlayer().getName())))
							return false;
					return true;
				}
			});
			if(giant != null){
				giant.interact("Attack");
				sleepUntil(new Condition(){
					public boolean verify(){
						return getLocalPlayer().isInCombat();
					}
				}, 2000);
			}
			break;
		case LOOT:
			GroundItem gi = getGroundItems().closest("Big bones", "Limpwurt root");
			if(gi != null){
				if(gi.isOnScreen()){
					gi.interact("Take");
					sleep(900,1200);
				}
				else{
					getWalking().walk(gi.getTile());
				}
			}
			break;
		case SLEEP:
			sleep(300,600);
			break;
		}
		return Calculations.random(300, 600);
	}

	public void onPaint(Graphics g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", 1, 11));
		g.drawString("Time Running: " + t.formatTime(), 25, 50);
		g.drawString("Experience(p/h): " + getSkillTracker().getGainedExperience(Skill.PRAYER) + "(" + getSkillTracker().getGainedExperiencePerHour(Skill.PRAYER) + ")", 25, 65);
		g.drawString("Level(gained): " + getSkills().getRealLevel(Skill.PRAYER) +"(" + getSkillTracker().getGainedLevels(Skill.PRAYER) + ")", 25, 80);
		if(state != null)
			g.drawString("State: " + state.toString(), 25, 95);
	}

	@Override
	public void onExit() {

	}
}