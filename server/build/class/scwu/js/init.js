var GLOBAL = {};

var SB = {
	execute : function(args) {
		var list = this._array_commands;
		
		if (args.length) {
			for (var i=0; i<args.length; i++) {
				list.push(args[i]);
			}
		}
		else {
			list.push(args);
		}
	},
	
	addItem : function(item) {
		var list = this._array_items;
		
		if (args.length) {
			for (var i=0; i<args.length; i++) {
				list.push(args[i]);
			}
		}
		else {
			list.push(args);
		}
	},
	
	getItems : function(pat) {
		
	},
	
	getUser : function(pat) {
		
	},
	
	_array_commands : [],
	_array_items : [],
	
	_pop_commands : function() {
		var ret = JSON.stringify(this._array_commands);
		this._array_commands = [];
		return ret;
	},
	
	_pop_items : function() {
		var ret = JSON.stringify(this._array_items);
		this._array_items = [];
		return ret;
	}
};