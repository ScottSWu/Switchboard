// Coup
// http://boardgamegeek.com/boardgame/131357/coup
// {"cmd":"exec","scripts":["scripts/coup.sb.js"]}
// {"cmd":"deal","items":"card.*","limit":2}

SB.execute({ "cmd" : "reset" });

var names = [];
for (var i=0; i<15; i++) {
	names.push("card_" + i);
}

var a,b,t;
for (var i=0; i<100; i++) {
	a = ~~(Math.random()*names.length);
	b = ~~(Math.random()*names.length);
	t = names[a];
	names[a] = names[b];
	names[b] = t;
}

var cards = [
	"ambassador","ambassador","ambassador",
	"assassin","assassin","assassin",
	"captain","captain","captain",
	"contessa","contessa","contessa",
	"duke","duke","duke"
];

var x,y;
for (var i=0; i<cards.length; i++) {
	x = i%5;
	y = ~~(i/5);
	SB.execute({
		"cmd" : "add",
		"name" : names[i] + "",
		"publicImage" : "items/coup/" + cards[i] + ".jpg",
		"privateImage" : "items/coup/back.jpg",
		"x" : 10 + 110*x, "y" : 10 + 140*y, "z" : 0, "w" : 90, "h" : 120,
		"publicMark" : true,
		"options" : [
			{	"label" : "Set public",
				"action" : (function() {
					SB.execute({
						"cmd" : "set",
						"target" : "item",
						"hash" : "%%ih",
						"property" : "publicVisibility",
						"value" : "true"
					});
				}) + ""
			},
			{	"label" : "Set private",
				"action" : (function() {
					SB.execute({
						"cmd" : "set",
						"target" : "item",
						"hash" : "%%ih",
						"property" : "publicVisibility",
						"value" : "false"
					});
				}) + ""
			},
			{	"label" : "Own",
				"action" : (function() {
					SB.execute({
						"cmd" : "set",
						"target" : "item",
						"hash" : "%%ih",
						"property" : "owner",
						"value" : "%%uh"
					});
				}) + ""
			},
			{	"label" : "Disown",
				"action" : (function() {
					SB.execute({
						"cmd" : "set",
						"target" : "item",
						"hash" : "%%ih",
						"property" : "owner",
						"value" : ""
					});
				}) + ""
			}
		]
	});
}

GLOBAL.coup = { coin : 1 };
SB.execute({
	"cmd" : "add",
	"name" : "coin_0",
	"publicImage" : "items/coup/coin.png",
	"x" : 560, "y" : 20, "z" : 1, "w" : 40, "h" : 40,
	"options" : [
		{	"label" : "Add one",
			"action" : (function() {
				SB.execute({
					"cmd" : "add",
					"name" : "coing_" + GLOBAL.coup.coin,
					"publicImage" : "items/coup/coin.png",
					"x" : 570,
					"y" : 30,
					"z" : 1,
					"w" : 40,
					"h" : 40,
				});
				
				GLOBAL.coup.coin++;
			}) + ""
		}
	]
});

for (var i=1; i<50; i++) {
	SB.execute({
		"cmd" : "add",
		"name" : "coin_" + i,
		"publicImage" : "items/coup/coin.png",
		"x" : 560, "y" : 20, "z" : 1, "w" : 40, "h" : 40
	});
}
