import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.Arrays;

public class SnakeLadderGame extends JFrame {
    private BoardPanel board;
    private ControlPanel ctrl;
    private Player[] players;
    private int curIdx;
    private boolean running;
    private boolean doubleTurn, hasShield, greenDice, primeMode;
    private int doubleCount;

    private SoundManager audio;
    private GlobalRankingManager rankMgr;
    private final int WIN_POS = 25;

    public SnakeLadderGame() {
        setTitle("☁️ Sky Adventure: Race to 25 ☁️");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        audio = new SoundManager();
        rankMgr = new GlobalRankingManager();
        board = new BoardPanel();
        ctrl = new ControlPanel();
        board.setControlPanel(ctrl);

        add(board, BorderLayout.CENTER);
        add(ctrl, BorderLayout.EAST);

        createMenu();

        ctrl.getVisualDice().setCallback(new VisualDice.DiceCallback() {
            public void onRollStart() {
                ctrl.enableDice(false);
                ctrl.setStatus("Rolling...", Color.GRAY);
                if(audio!=null) audio.playDiceRoll();
            }
            public void onRollFinished() { processTurn(); }
        });

        setSize(1000, 700);
        setLocationRelativeTo(null);
        showSetup();
    }

    private void createMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu m = new JMenu("Game");
        JMenuItem n = new JMenuItem("New Game");
        n.addActionListener(e -> showSetup());

        JMenuItem r = new JMenuItem("🏆 Global Stats (Aktif)");
        // FIX ERROR DI SINI: Tambahkan parameter false (Mode Aktif)
        r.addActionListener(e -> new RecordDisplayDialog(this, rankMgr, false).setVisible(true));

        m.add(n); m.add(r);
        bar.add(m);
        setJMenuBar(bar);
    }

    private void showSetup() {
        if(audio!=null) audio.playSetupMusic();
        String[] names = PlayerDialog.showPlayerDialog(this, rankMgr);
        if(names!=null) {
            initGame(names);
        } else {
            if(audio!=null) audio.stopBgm();
        }
    }

    private void initGame(String[] names) {
        Color[] cols = {Color.RED, Color.BLUE, new Color(0,100,0), Color.ORANGE};
        players = new Player[names.length];
        for(int i=0; i<names.length; i++) players[i] = new Player(names[i], cols[i%4], i);

        board.setPlayers(players);
        board.resetSpecialNodes();
        curIdx = 0; running = true;
        resetFlags();

        ctrl.clearLog();
        ctrl.addLog("=== SKY ADVENTURE START ===");
        ctrl.addLog("Goal: Reach step "+WIN_POS);
        ctrl.addLog("Collect ⭐ Stars for high score!");

        if(audio!=null) audio.playInGameMusic();
        updateUI();
    }

    private void resetFlags() {
        doubleTurn=false; hasShield=false; greenDice=false; primeMode=false; doubleCount=0;
    }

    private void processTurn() {
        if(!running) return;
        Player p = players[curIdx];

        VisualDice.Result res;
        if(greenDice) {
            res = new VisualDice.Result(ctrl.getVisualDice().rollLogic().number, true);
            greenDice = false;
            ctrl.addLog("🍀 Green Dice Used!");
        } else {
            res = ctrl.getVisualDice().rollLogic();
        }

        ctrl.getVisualDice().setFinalResult(res.number);
        String dir = res.isGreen ? "MAJU" : "MUNDUR";
        ctrl.setStatus(dir + " " + res.number, res.isGreen ? new Color(0,150,0) : Color.RED);

        int oldPos = p.getPosition();
        if(primeMode) {
            ctrl.addLog("🔥 Prime Power: Seeking Portal...");
            primeMove(p, oldPos, res);
            primeMode = false;
        } else {
            int newPos = calcPos(oldPos, res);
            ctrl.addLog(p.getName() + " " + dir + " " + res.number);

            board.animate(p, oldPos, newPos, () -> {
                p.setPosition(newPos);
                int star = board.getScore(newPos);
                if(star > 0) {
                    p.addScore(star);
                    board.takeScore(newPos);
                    ctrl.addLog("⭐ Collected " + star + " stars!");
                    if(audio!=null) audio.playDiceRoll();
                }

                if(board.getSpecialNodes().contains(newPos)) checkSpecial(p, newPos, res);
                else checkEnd(res);

                if(board.isPrime(newPos) && newPos>1) {
                    ctrl.addLog("🔴 Landed on Prime! Power ready.");
                    primeMode = true;
                }
            });
        }
    }

    private int calcPos(int cur, VisualDice.Result r) {
        int s = r.number;
        if(hasShield && !r.isGreen) { hasShield=false; ctrl.addLog("🛡️ Shield used!"); return Math.min(WIN_POS, cur+s); }
        return r.isGreen ? Math.min(WIN_POS, cur+s) : Math.max(0, cur-s);
    }

    private void primeMove(Player p, int start, VisualDice.Result res) {
        Integer target = board.getNearestSpecial(start);
        if(target!=null && Math.abs(target-start) <= res.number) {
            board.animate(p, start, target, () -> {
                p.setPosition(target);
                checkSpecial(p, target, res);
            });
        } else {
            int np = calcPos(start, res);
            board.animate(p, start, np, () -> { p.setPosition(np); checkEnd(res); });
        }
    }

    private void checkSpecial(Player p, int pos, VisualDice.Result r) {
        int rnd = new Random().nextInt(5);
        if(rnd==0) {
            Integer t = board.getRandomSpecial(pos);
            if(t!=null) {
                ctrl.addLog("🚀 PORTAL: Teleport to "+t);
                board.animate(p, pos, t, () -> { p.setPosition(t); checkEnd(r); });
                return;
            }
        } else if(rnd==1) { doubleTurn=true; doubleCount=1; ctrl.addLog("🔄 Extra Turn!"); }
        else if(rnd==2) { greenDice=true; ctrl.addLog("🍀 Next Dice Green!"); }
        else if(rnd==3) { hasShield=true; ctrl.addLog("🛡️ Shield Get!"); }
        else { p.addScore(50); ctrl.addLog("⭐ Bonus 50 Stars!"); }
        checkEnd(r);
    }

    private void checkEnd(VisualDice.Result r) {
        Player p = players[curIdx];
        if(p.getPosition() >= WIN_POS) {
            running = false;
            ctrl.setStatus("WINNER!", Color.MAGENTA);
            ctrl.addLog("🏆 WINNER: " + p.getName());

            if(audio!=null) audio.playWinSound();

            // Simpan Record
            rankMgr.addRecord(new GameRecord(Arrays.asList(players), p.getName()));

            // Tampilkan Dialog dengan Opsi Continue
            new RankingDialog(this, players, new RankingDialog.DialogAction() {
                @Override
                public void onContinue() { continueGame(); }
                @Override
                public void onMainMenu() { showSetup(); }
            }).setVisible(true);

            return;
        }

        if(r.isMultipleOfFive() && !doubleTurn) { doubleTurn=true; doubleCount=1; ctrl.addLog("🎉 Rolled 5! Double Turn!"); }

        if(doubleTurn) {
            if(doubleCount < 2) { doubleCount++; }
            else { doubleTurn=false; doubleCount=0; nextPlayer(); }
        } else {
            nextPlayer();
        }
        updateUI();
    }

    private void continueGame() {
        for(Player p : players) p.setPosition(0); // Reset Posisi, Skor Tetap
        board.resetSpecialNodes();
        curIdx = 0; running = true;
        resetFlags();
        ctrl.clearLog();
        ctrl.addLog("=== NEXT ROUND ===");
        ctrl.addLog("Scores carried over!");
        if(audio!=null) audio.playInGameMusic();
        updateUI();
    }

    private void nextPlayer() { curIdx = (curIdx+1)%players.length; }

    private void updateUI() {
        Player p = players[curIdx];
        String info = p.getName() + (doubleTurn?" (2x Turn)":"") + (hasShield?" 🛡️":"") + (greenDice?" 🍀":"");
        ctrl.setCurPlayer(info);
        ctrl.updateRank(players);
        ctrl.enableDice(true);
        board.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SnakeLadderGame().setVisible(true));
    }
}