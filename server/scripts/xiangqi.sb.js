// Xiang Qi
// {"cmd":"exec","scripts":["scripts/xiangqi.sb.js"]}

SB.execute({ "cmd" : "reset" });

SB.execute({
	"cmd" : "add",
	"name" : "xiangqi_board",
	"publicImage" : "http://switchboard.ga/items/xiangqi/board.png",
	"x" : 104,
	"y" : 0,
	"z" : -1,
	"w" : 432,
	"h" : 480
});

// Pawns
for (var i=0; i<5; i++) {
	// Red
	SB.execute({
		"cmd" : "add",
		"name" : "r_pawn_" + i,
		"publicImage" : "http://switchboard.ga/items/xiangqi/red_bing.png",
		"x" : 121 + 90*i, "y" : 288, "z" : 0, "w" : 40, "h" : 40
	});
	
	// Black
	SB.execute({
		"cmd" : "add",
		"name" : "b_zu_" + i,
		"publicImage" : "http://switchboard.ga/items/xiangqi/black_zu.png",
		"x" : 121 + 90*i, "y" : 153, "z" : 0, "w" : 40, "h" : 40
	});
}

// Pieces
var rpieces = [ "ju","ma","xiang","shi","shuai","shi","xiang","ma","ju" ];
var bpieces = [ "ju","ma","xiang","shi","jiang","shi","xiang","ma","ju" ];
for (var i=0; i<9; i++) {
	// Red
	SB.execute({
		"cmd" : "add",
		"name" : "r_" + rpieces[i] + "_" + i,
		"publicImage" : "http://switchboard.ga/items/xiangqi/red_" + rpieces[i] + ".png",
		"x" : 121 + 45*i, "y" : 423, "z" : 0, "w" : 40, "h" : 40
	});
	
	// Black
	SB.execute({
		"cmd" : "add",
		"name" : "b_" + bpieces[i] + "_" + i,
		"publicImage" : "http://switchboard.ga/items/xiangqi/black_" + bpieces[i] + ".png",
		"x" : 121 + 45*i, "y" : 17, "z" : 0, "w" : 40, "h" : 40
	});
}

// Cannons
SB.execute({
	"cmd" : "add",
	"name" : "r_pao_1",
	"publicImage" : "http://switchboard.ga/items/xiangqi/red_pao.png",
	"x" : 167, "y" : 333, "z" : 0, "w" : 40, "h" : 40
});
SB.execute({
	"cmd" : "add",
	"name" : "r_pao_2",
	"publicImage" : "http://switchboard.ga/items/xiangqi/red_pao.png",
	"x" : 435, "y" : 333, "z" : 0, "w" : 40, "h" : 40
});
SB.execute({
	"cmd" : "add",
	"name" : "r_pao_1",
	"publicImage" : "http://switchboard.ga/items/xiangqi/black_pao.png",
	"x" : 167, "y" : 107, "z" : 0, "w" : 40, "h" : 40
});
SB.execute({
	"cmd" : "add",
	"name" : "r_pao_2",
	"publicImage" : "http://switchboard.ga/items/xiangqi/black_pao.png",
	"x" : 435, "y" : 107, "z" : 0, "w" : 40, "h" : 40
});
