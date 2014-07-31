// Chess
// {"cmd":"exec","scripts":["scripts/chess.sb.js"]}

SB.execute({ "cmd" : "reset" });

SB.execute({
	"cmd" : "add",
	"name" : "chess_board",
	"publicImage" : "http://switchboard.ga/items/chess/board.png",
	"x" : 80,
	"y" : 0,
	"z" : -1,
	"w" : 480,
	"h" : 480
});

var pieces = [ "rook","knight","bishop","queen","king","bishop","knight","rook" ];

for (var i=0; i<8; i++) {
	 // White
	SB.execute({
		"cmd" : "add",
		"name" : "w_pawn_" + i,
		"publicImage" : "http://switchboard.ga/items/chess/white_pawn.png",
		"x" : 116 + 51*i,
		"y" : 342,
		"z" : 0,
		"w" : 48,
		"h" : 48
	});
	SB.execute({
		"cmd" : "add",
		"name" : "w_" + pieces[i] + "_" + i,
		"publicImage" : "http://switchboard.ga/items/chess/white_" + pieces[i] + ".png",
		"x" : 116 + 51*i,
		"y" : 393,
		"z" : 0,
		"w" : 48,
		"h" : 48
	});
	
	// Black
	SB.execute({
		"cmd" : "add",
		"name" : "b_pawn_" + i,
		"publicImage" : "http://switchboard.ga/items/chess/black_pawn.png",
		"x" : 116 + 51*i,
		"y" : 90,
		"z" : 0,
		"w" : 48,
		"h" : 48
	});
	SB.execute({
		"cmd" : "add",
		"name" : "b_" + pieces[i] + "_" + i,
		"publicImage" : "http://switchboard.ga/items/chess/black_" + pieces[i] + ".png",
		"x" : 116 + 51*i,
		"y" : 39,
		"z" : 0,
		"w" : 48,
		"h" : 48
	});
}
