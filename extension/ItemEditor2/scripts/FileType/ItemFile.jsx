import File from "../File";

export default class ItemFile{

	static type = "物品";
	static path = () => window.localStorage["path"] + "/script/data/item/";

	static typeMap = [
		{name: "道具", type: "item", color: "#7b94a0"},
		{name: "装备", type: "equipment", color: "#7ba087"},
		{name: "符卡", type: "spellcard", color: "#8fa07b"},
		{name: "料理", type: "cooking", color: "#9b7ba0"},
		{name: "任务道具", type: "task", color: "#7c7ba0"},
		{name: "材料", type: "material", color: "#a0907b"},
		{name: "笔记", type: "note", color: "#a07b7b"}
	]

	static list(callback) {
		File.list(this.path(), files => {
			this.files = files.map(e => {return {name: e, type: this.type, fileName: e, text: "", path: this.path() + e, errorFormat: false}});
			callback(files);
		});
	}

	static read(name, callback){
		File.read(this.path() + name, e => {
			let result = {errorFormat: false, fileText: e, label: "", prefix: null};

			try{
				let obj = eval("(" + e + ")");
				result.label = obj.name;

				let type = this.findType(obj.type);
				result.prefix = {
					text: type.name,
					color: type.color
				};

			}catch(e){
				result.label = "<无法解析>";
				result.errorFormat = true;
			}

			callback(result);
		});
	}

	static findType(str){
		for(let type of this.typeMap)
			if(type.name.toLocaleLowerCase().indexOf(str.toLocaleLowerCase()) >= 0 || type.type.toLocaleLowerCase().indexOf(str.toLocaleLowerCase()) >= 0)
				return type;

			return {name: "", type: ""};
	}

	toggleSearch() {
		console.log("?");
	}
}