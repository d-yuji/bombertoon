package client;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import Common.ColorPair;
import Common.Setting;
import server.Color;

public class GameClient extends BasicGameState {
	FieldClient[][] fc;
	PlayerClient[] pc;
	private int state;
	private boolean isMove;
	public static final int PLAYERNUMBER = Common.Setting.P;
	public static final int FIELDHEIGHT = 16;
	public static final int FIELDWIDTH = 16;
	private boolean isColorDecided = false;
	private int moveTimer;
	private int gameSetTimer;// ←0になったら画面遷移 TODO さーばー側からの指令で画面遷移するようにする
	private UnicodeFont uf;
	int[] fieldData;
	Sound explosionSound;
	Music battle;
	Image[] up = new Image[PLAYERNUMBER];// キャラ画像
	Image[] down = new Image[PLAYERNUMBER];// キャラ画像
	Image[] right = new Image[PLAYERNUMBER];// キャラ画像
	Image[] left = new Image[PLAYERNUMBER];// キャラ画像
	Image muteki[] = new Image[PLAYERNUMBER];
	Image ink[] = new Image[PLAYERNUMBER];
	Image stage;
	Image bomb;
	Image explosion;
	Image unbreakable;
	Image breakable;
	Image back;
	static final int OFFSET_Y = 40;
	static final int OFFSET_Y_BOMB = -8;
	static final int OFFSET_X_EXPLOSION = -8;
	static final int OFFSET_Y_EXPLOSION = -35;
	static final int OFFSET_X_OBJECT = 5;
	static final int OFFSET_Y_OBJECT = 55;
	private static TransmissionClient tc;
	public GameClient(int state) {
		this.state = state;
	}

	public void init(GameContainer arg0, StateBasedGame arg1) throws SlickException {
		battle = new Music("BGM\\Battle.ogg");
		explosionSound = new Sound("SE\\bomb1.ogg");
		uf = new UnicodeFont("SHOWG.TTF", 55, false, false);
		back = new Image("Map\\batlle_background.png");
		if (uf != null)
			System.out.println("good");
		fieldData = Setting.STAGE4TWO; // フィールドのパラメータ

		ink[0] = new Image("Map\\ink\\" + Common.Setting.ColorTeam1.toString().toLowerCase() + " (1).png");
		ink[1] = new Image("Map\\ink\\" + Common.Setting.ColorTeam2.toString().toLowerCase() + " (1).png");
		unbreakable = new Image("Steel.png");
		breakable = new Image("Wood.png");
		stage = new Image("stage.png");
		explosion = new Image("explosion.png");
		bomb = new Image("bom_gif.gif");

		Color iro;
		for (int i = 0; i < PLAYERNUMBER; i++) { // キャラ画像の初期化(キャラ画像にはそいつのインクの色、無敵エリアの色も含まれる)

			switch (i) {
			case 2: // 暫定的な色分け TODO 真面目な分け
				iro = Common.Setting.ColorTeam1;
				System.out.println("2で初期化されたで");
				break;

			case 0:
				iro = Common.Setting.ColorTeam1;
				System.out.println("0で初期化されたで" + iro.toString() + "色やで");
				break;
			case 3:
				iro = Common.Setting.ColorTeam2;
				System.out.println("case3で初期化されたで" + iro.toString() + "色やで");
				break;
			case 1:
				iro = Common.Setting.ColorTeam2;
				System.out.println("case1で初期化されたで" + iro.toString() + "色やで");
				break;
			default:
				System.out.println("defaultで初期化されたで");
				iro = Color.Transparent;
				break;
			}
			down[i] = new Image(
					"players\\" + iro.toString() + "\\" + iro.toString().toLowerCase() + "_player" + "_1.png");
			up[i] = new Image(
					"players\\" + iro.toString() + "\\" + iro.toString().toLowerCase() + "_player" + "_2.png");
			left[i] = new Image(
					"players\\" + iro.toString() + "\\" + iro.toString().toLowerCase() + "_player" + "_3.png");
			right[i] = new Image(
					"players\\" + iro.toString() + "\\" + iro.toString().toLowerCase() + "_player" + "_4.png");
			muteki[i] = new Image("Map\\stage\\Steel\\muteki\\" + iro.toString().toLowerCase() + "_stage_muteki.png");
			// ink[i] = new Image("Map\\ink\\" + iro.toString().toLowerCase() +
			// " (1).png");
		}
		moveTimer = -50;
	}

	/* 配列の座標から画面の座標に変換 */
	private float xyToDispX(int x, int y) {
		return 77f + x * 45f + y * -4f;
	}

	private float xyToDispY(int x, int y) {
		return 110f + y * 25f;
	}

	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics gr) throws SlickException {
		// field wo byou-ga, field wo ichi ban shita ni hyouji saseru tame

		back.draw(0, 0);
		for (int y = 0; y < FIELDHEIGHT; y++) {
			for (int x = 0; x < FIELDWIDTH; x++) {

				// 座標を取ってくる
				float dispX = xyToDispX(x, y);
				float dispY = xyToDispY(x, y);
				if (fieldData[x + y * FIELDWIDTH] < 0) {
					muteki[fieldData[x + y * FIELDWIDTH] * (-1) - 1].draw(dispX, dispY);
				} else
					stage.draw(dispX, dispY);

			}
		}

		// 色を塗る ここから↓
		for (int y = 0; y < FIELDHEIGHT; y++) {
			for (int x = 0; x < FIELDWIDTH; x++) {

				// 座標を取ってくる
				float dispX = xyToDispX(x, y);
				float dispY = xyToDispY(x, y);
				if ((fc[y][x].status != Status.MUTEKI)) { // 無敵の場所にも色がある→しかしそこはインクはいらない

					if (fc[y][x].color == Color.Transparent) {
						// TODO debug 6/21 okmt: log見るために一度コメントアウトしたよ
					} else if (fc[y][x].color == Common.Setting.ColorTeam1) {
						ink[0].draw(dispX, dispY);
					} else if (fc[y][x].color == Common.Setting.ColorTeam2) {
						ink[1].draw(dispX, dispY);
					}
				}
			}
		}

		// 色を塗る ここまで↑

		for (int y = 0; y < FIELDHEIGHT; y++) {
			for (int x = 0; x < FIELDWIDTH; x++) {

			}
		}
		// 障害物を描画 ここから↓
		for (int y = 0; y < FIELDHEIGHT; y++) {
			for (int x = 0; x < FIELDWIDTH; x++) {

				// 座標を取ってくる
				float dispX = xyToDispX(x, y);
				float dispY = xyToDispY(x, y);
				switch (fc[y][x].status) {
				case UNBREAKABLE:
					unbreakable.draw(dispX - OFFSET_X_OBJECT, dispY - OFFSET_Y_OBJECT);
					break;
				case BREAKABLE1:
					breakable.draw(dispX - OFFSET_X_OBJECT, dispY - OFFSET_Y_OBJECT);
					break;
				default:

				}
				dispX = xyToDispX(x, y);
				dispY = xyToDispY(x, y) - OFFSET_Y;

				// 爆弾を配置
				if (fc[y][x].isExistBomb == true) {

					bomb.draw(dispX, dispY - OFFSET_Y_BOMB);
				}
				if (fc[y][x].status == Status.BOMBERING) {
					explosion.draw(dispX - OFFSET_X_EXPLOSION, dispY - OFFSET_Y_EXPLOSION);
				}

				for (int i = 0; i < PLAYERNUMBER; i++) { // キャラクターの表示
															// 向きにおうじて画像を変える
					if (!pc[i].isDeath && pc[i].x == x && pc[i].y == y) {
						dispX = xyToDispX(pc[i].x, pc[i].y);
						dispY = xyToDispY(pc[i].x, pc[i].y) - OFFSET_Y; // 20:offset

						switch (pc[i].dir) {
						case UP:
							up[i].draw(dispX, dispY);
							break;
						case DOWN:
							down[i].draw(dispX, dispY);
							break;
						case RIGHT:
							right[i].draw(dispX, dispY);
							break;
						case LEFT:
							left[i].draw(dispX, dispY);
							break;

						default:
							break;
						}
					}

				}

			}
		}
		uf.addAsciiGlyphs();
		uf.getEffects().add(new ColorEffect());
		uf.loadGlyphs();
		gr.setFont(uf);
		// 障害物を描画 ここまで↑
		gr.drawString(Integer.toString(gameSetTimer / 1000), 400, 30);

	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
		for (int y = 0; y < FIELDHEIGHT; y++) {
			for (int x = 0; x < FIELDWIDTH; x++) {

				if (fc[y][x].bombCount > 0 && (fc[y][x].isExistBomb)) {
					fc[y][x].bombCount -= delta;
				}
				if (fc[y][x].bombCount <= 0) {
					explosionSound.play();
					fc[y][x].bombCount = 2800;
				}
			}
		}
		if (tc == null)
			return;
		if (!tc.isReady()) {
			return;
		}
		if (tc.recieveFinish()) {

			sbg.enterState(State.RESULT);
		}
		if (isColorDecided == false) {
			Setting.setTeamColor(ColorPair.getColorPair(tc.recieveColorPair()));
			ink[0] = new Image("Map\\ink\\" + Common.Setting.ColorTeam1.toString().toLowerCase() + " (1).png");
			ink[1] = new Image("Map\\ink\\" + Common.Setting.ColorTeam2.toString().toLowerCase() + " (1).png");
			Color iro;
			for (int i = 0; i < PLAYERNUMBER; i++) { // キャラ画像の初期化(キャラ画像にはそいつのインクの色、無敵エリアの色も含まれる)

				switch (i) {
				case 2: // 暫定的な色分け TODO 真面目な分け
					iro = Common.Setting.ColorTeam1;
					System.out.println("2で初期化されたで");
					break;

				case 0:
					iro = Common.Setting.ColorTeam1;
					ink[0] = new Image("Map\\ink\\" + iro.toString().toLowerCase() + " (1).png");

					System.out.println("0で初期化されたで" + iro.toString() + "色やで");
					break;
				case 3:
					iro = Common.Setting.ColorTeam2;
					ink[1] = new Image("Map\\ink\\" + iro.toString().toLowerCase() + " (1).png");

					System.out.println("case3で初期化されたで" + iro.toString() + "色やで");
					break;
				case 1:
					iro = Common.Setting.ColorTeam2;
					System.out.println("case1で初期化されたで" + iro.toString() + "色やで");
					break;
				default:
					System.out.println("defaultで初期化されたで");
					iro = Color.Transparent;
					break;
				}
				down[i] = new Image(
						"players\\" + iro.toString() + "\\" + iro.toString().toLowerCase() + "_player" + "_1.png");
				up[i] = new Image(
						"players\\" + iro.toString() + "\\" + iro.toString().toLowerCase() + "_player" + "_2.png");
				left[i] = new Image(
						"players\\" + iro.toString() + "\\" + iro.toString().toLowerCase() + "_player" + "_3.png");
				right[i] = new Image(
						"players\\" + iro.toString() + "\\" + iro.toString().toLowerCase() + "_player" + "_4.png");
				muteki[i] = new Image(
						"Map\\stage\\Steel\\muteki\\" + iro.toString().toLowerCase() + "_stage_muteki.png");
			}
			isColorDecided = true;
		}
		// 一定時間入力を受け付けないことで移動をゆっくりにしている
		if (isMove == false) {
			moveTimer -= delta;
			if (moveTimer < 0) {
				isMove = true;
			}
		}
		if (isMove == true) { // TODO 死んでるときは入力を受け付けない
			if (gc.getInput().isKeyDown(Input.KEY_UP) && gc.getInput().isKeyDown(Input.KEY_SPACE)) {
				tc.sendHuman(Direction.TURN_UP);
			} else if (gc.getInput().isKeyDown(Input.KEY_DOWN) && gc.getInput().isKeyDown(Input.KEY_SPACE)) {
				tc.sendHuman(Direction.TURN_DOWN);
			} else if (gc.getInput().isKeyDown(Input.KEY_RIGHT) && gc.getInput().isKeyDown(Input.KEY_SPACE)) {
				tc.sendHuman(Direction.TURN_RIGHT);
			} else if (gc.getInput().isKeyDown(Input.KEY_LEFT) && gc.getInput().isKeyDown(Input.KEY_SPACE)) {
				tc.sendHuman(Direction.TURN_LEFT);
			} else if (gc.getInput().isKeyDown(Input.KEY_UP)) {
				tc.sendHuman(Direction.UP);
			} else if (gc.getInput().isKeyDown(Input.KEY_DOWN)) {
				tc.sendHuman(Direction.DOWN);
			} else if (gc.getInput().isKeyDown(Input.KEY_RIGHT)) {
				tc.sendHuman(Direction.RIGHT);
			} else if (gc.getInput().isKeyDown(Input.KEY_LEFT)) {
				tc.sendHuman(Direction.LEFT);
			} else if (gc.getInput().isKeyDown(Input.KEY_Z)) {
				tc.sendBomb();
			}
			isMove = false;
			moveTimer = 80; // コマンド入れてから 80m秒? は次のコマンドを受け付けない
		}
		// フィールドが変化していたら更新
		FieldClient f;
		while ((f = tc.recieveField()) != null) {
			fc[f.y][f.x] = f;
		}

		// キャラクタの位置などをを更新
		PlayerClient p;
		while ((p = tc.recievedHuman()) != null) {

			pc[p.playerID] = p;
		}
		gameSetTimer = tc.receiveTime();
	}

	@Override
	public void enter(GameContainer container, StateBasedGame game) throws SlickException {
		battle.loop();
		fc = new FieldClient[FIELDWIDTH][FIELDHEIGHT];
		pc = new PlayerClient[PLAYERNUMBER];
		gameSetTimer = 1000;
		// ゲームが始まったときに初期化
		for (int i = 0; i < PLAYERNUMBER; i++) {
			switch (i) {
			case 0:
				pc[i] = new PlayerClient(0, 0, 0, Direction.DOWN, false);
				break;
			case 1:
				pc[i] = new PlayerClient(1, FIELDWIDTH - 1, 0, Direction.DOWN, false);
				break;
			case 2:
				pc[i] = new PlayerClient(2, FIELDWIDTH - 1, FIELDHEIGHT - 1, Direction.DOWN, false);
				break;
			case 3:
				pc[i] = new PlayerClient(3, 0, FIELDHEIGHT - 1, Direction.DOWN, false);
				break;

			}
		}

		for (int y = 0; y < FIELDHEIGHT; y++) {
			for (int x = 0; x < FIELDWIDTH; x++) {
				switch (fieldData[x + y * FIELDWIDTH]) {
				case 0:
					fc[y][x] = new FieldClient(x, y, false, Status.NOTHING, Color.Transparent);// TODO
					break;
				case 1:
					fc[y][x] = new FieldClient(x, y, false, Status.BREAKABLE1, Color.Transparent);// TODO
					break;
				case 2:
					fc[y][x] = new FieldClient(x, y, false, Status.UNBREAKABLE, Color.Transparent);// TODO
					break;
				default:
					fc[y][x] = new FieldClient(x, y, false, Status.NOTHING, Color.Transparent);// TODO
					break;

				} // 無色を追加
			}
		}

		if (ClientStarter.getTransmissionClient() == null) // null出なくなるまで待つ
			System.err.println("GameClient:tc null");

		tc = ClientStarter.getTransmissionClient();

		super.enter(container, game);
	}

	@Override
	public void leave(GameContainer container, StateBasedGame game) throws SlickException {
		// TODO 自動生成されたメソッド・スタブ
		battle.stop();
	}

	@Override
	public int getID() {
		return state;
	}

}
