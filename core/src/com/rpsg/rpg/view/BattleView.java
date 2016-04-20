package com.rpsg.rpg.view;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.rpsg.gdxQuery.$;
import com.rpsg.rpg.core.RPG;
import com.rpsg.rpg.core.Setting;
import com.rpsg.rpg.object.base.BattleParam;
import com.rpsg.rpg.object.base.BattleRes;
import com.rpsg.rpg.object.base.items.BattleContext;
import com.rpsg.rpg.object.base.items.BattleResult;
import com.rpsg.rpg.object.base.items.Spellcard;
import com.rpsg.rpg.object.rpg.Enemy;
import com.rpsg.rpg.object.rpg.Hero;
import com.rpsg.rpg.system.base.Res;
import com.rpsg.rpg.system.ui.Animations;
import com.rpsg.rpg.system.ui.DefaultIView;
import com.rpsg.rpg.system.ui.EnemyGroup;
import com.rpsg.rpg.system.ui.HeroStatusGroup;
import com.rpsg.rpg.system.ui.Image;
import com.rpsg.rpg.system.ui.Progress;
import com.rpsg.rpg.system.ui.Status;
import com.rpsg.rpg.system.ui.TextButton;
import com.rpsg.rpg.system.ui.Timer;
import com.rpsg.rpg.utils.game.GameUtil;
import com.rpsg.rpg.utils.game.TimeUtil;

public class BattleView extends DefaultIView{
	
	BattleParam param;
	public HeroStatusGroup heroGroup;
	public EnemyGroup enemyGroup;
	public Status status;
	Animations animations = new Animations(this);
	Timer timer;
	Progress p;
	
	public BattleView(BattleParam param) {
		this.param = param;
		stage = new Stage(new ScalingViewport(Scaling.stretch, GameUtil.screen_width, GameUtil.screen_height, new OrthographicCamera()));
	}
	
	@Override
	public BattleView init() {
		stage.clear();
		
		$.add(Res.get(Setting.UI_BASE_IMG).size(1024,576).color(.5f,.5f,.5f,1)).appendTo(stage);//TODO debug;
		
		$.add(status = new Status()).setPosition(0, 0).appendTo(stage);
		
		ArrayList<Hero> heros = RPG.ctrl.hero.currentHeros();
		
		heroGroup = new HeroStatusGroup(heros);
		$.add(heroGroup).appendTo(stage).setPosition(0, 0);
		
		enemyGroup = new EnemyGroup(param.enemy);
		$.add(enemyGroup).appendTo(stage).setPosition(GameUtil.screen_width/2 - enemyGroup.getWidth()/2, GameUtil.screen_height/2 - enemyGroup.getHeight()/2 + 50).setAlign(Align.center);
		
		
		$.add(new TextButton("结束战斗！",BattleRes.textButtonStyle)).appendTo(stage).setPosition(100,170).onClick(RPG.ctrl.battle::stop);
		
		$.add(timer = new Timer(heros,enemyGroup.list(),this::onTimerToggle)).appendTo(stage);
		
		status.add("fuck you");
		stage.setDebugAll(!false);
		
		$.add(Res.get(Setting.UI_BASE_IMG).size(GameUtil.screen_width,GameUtil.screen_height).color(0,0,0,1)).appendTo(stage).addAction(Actions.sequence(Actions.fadeOut(.3f,Interpolation.pow2In),Actions.removeActor()));
		
		status.setZIndex(999999);
		
		stage.addActor(animations);
		
		return this;
	}
	
	public void onTimerToggle(Object obj){
		timer.pause(true);
		String name = obj instanceof Enemy ? ((Enemy)obj).name : ((Hero)obj).name;
		
		(obj instanceof Enemy ? ((Enemy)obj).target : ((Hero)obj).target).nextTurn();
		
		status.add("[#ff7171]"+name+"[] 的战斗回合");
		
		if(obj instanceof Hero){
			Hero hero = (Hero)obj;
			Image fg = $.add(Res.get(Setting.IMAGE_FG+hero.fgname+"/Normal.png")).appendTo(stage).setScaleX(-0.33f).setScaleY(0.33f).setOrigin(Align.bottomLeft).setPosition(GameUtil.screen_width+500, 0).addAction(Actions.moveBy(-400, 0,1f,Interpolation.pow4Out)).setZIndex(1).getItem(Image.class);
			Table menu = $.add(new Table()).appendTo(stage).setPosition(600, 220).getItem(Table.class);
			
			Runnable stopCallback = ()->{
				fg.remove();
				menu.remove();
				timer.pause(false);
			};
			
			menu.add(new TextButton("攻击",BattleRes.textButtonStyle).onClick(()->{
				attack(hero,stopCallback);
			}));
			
			menu.add(new TextButton("防御",BattleRes.textButtonStyle).onClick(()->{
				define(hero,stopCallback);
			}));
			
			menu.add(new TextButton("符卡",BattleRes.textButtonStyle).onClick(()->{
				RPG.ctrl.battle.stop();
				stopCallback.run();
			}));
			
			menu.add(new TextButton("物品",BattleRes.textButtonStyle).onClick(()->{
				RPG.ctrl.battle.stop();
				stopCallback.run();
			}));
			
			menu.add(new TextButton("逃跑",BattleRes.textButtonStyle).onClick(()->{
				escape(hero,()->{
					menu.remove();
					stopCallback.run();
				});
			}));
			
			$.each(menu.getCells(), cell -> cell.size(150,30));
		}else{
			Enemy enemy = (Enemy)obj;
			BattleResult result = enemy.act(new BattleContext(enemy, null, (List<?>) enemyGroup.list().clone(), (List<?>) RPG.ctrl.hero.currentHeros.clone()));
			animations.play(result,() -> timer.pause(false));
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		logic();
		stage.draw();
	}

	@Override
	public void logic() {
		stage.act();
	}
	
	@Override
	public void onkeyDown(int keyCode) {
		if(keyCode == Keys.R) init();
		if(keyCode == Keys.S) status.add("随便说一句话："+Math.random());
		if(keyCode == Keys.D) status.append(" & "+Math.random());
		if(keyCode == Keys.F) status.append("[#ffaabb]彩色测试[]");
		if(keyCode == Keys.P) {
			RPG.ctrl.hero.currentHeros.get(0).target.setProp("hp", MathUtils.random(0,100));
			RPG.ctrl.hero.currentHeros.get(0).target.setProp("mp", MathUtils.random(0,100));
			RPG.ctrl.hero.currentHeros.get(1).target.setProp("hp", MathUtils.random(0,100));
			RPG.ctrl.hero.currentHeros.get(1).target.setProp("mp", MathUtils.random(0,100));
		}
		super.onkeyDown(keyCode);
	}
	
	public void escape(Hero hero,Runnable callback){
		double random = Math.random();
		boolean flag = random > .5;
		status.add(hero.getName()+" 尝试逃跑").append(".",5).append(".",10).append(".",15);
		TimeUtil.add(()->{
			status.append(flag ? "成功了" : "但是失败了");
			callback.run();
		},700);
	}
	
	private void define(Hero hero,Runnable callback){
		status.add(hero.name + "展开了防御的姿态");
		BattleResult result = Spellcard.defense().use(new BattleContext(hero, null,(List<?>) RPG.ctrl.hero.currentHeros.clone(), (List<?>) enemyGroup.list().clone()));
		animations.play(result, ()->{
			callback.run();
		});
	}
	
	private void attack(Hero hero,Runnable callback){
		enemyGroup.select((enemy)->{
			status.add("攻击了 " + enemy.name);
			BattleResult result = Spellcard.attack().use(new BattleContext(hero, enemy,(List<?>) RPG.ctrl.hero.currentHeros.clone(), (List<?>) enemyGroup.list().clone()));
			animations.play(result,()->{
				callback.run();
			});
			if(enemy.target.isDead()){
				status.add(enemy.name + "已死亡");
				enemyGroup.remove(enemy);
				timer.remove(enemy);
			}
		});
	}

}
