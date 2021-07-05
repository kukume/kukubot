package me.kuku.simbot.pojo;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public enum CodeType {
	PHPFiveThree("php"),
	PHPFiveFour("php5.4"),
	PHPFiveFive("php5.5"),
	PHPFiveSix("php5.6"),
	PHPSeven("php7"),
	PHPSevenFour("php7.4"),
	PythonTwo("python"),
	PythonThree("python3"),
	Csharp("csharp"),
	Fsharp("fsharp"),
	JavaSeven("java1.7"),
	JavaEight("java"),
	Shell("shell"),
	C("c"),
	Cpp("cpp"),
	Nasm("nasm"),
	Go("go"),
	Lua("lua"),
	Perl("perl"),
	Ruby("ruby"),
	Nodejs("nodejs"),
	ObjectiveC("objective-c"),
	Swift("swift"),
	Erlang("erlang"),
	Rust("rust"),
	R("r"),
	Scala("scala"),
	Haskell("haskell"),
	D("d"),
	Clojure("clojure"),
	Groovy("groovy"),
	Lisp("lisp"),
	Ocaml("ocaml"),
	CoffeeScript("coffee"),
	Racket("racket"),
	Nim("nim");

	private final String type;

	CodeType(String type){
		this.type = type;
	}

	public static CodeType parse(String type){
		CodeType[] values = CodeType.values();
		for (CodeType value : values) {
			if (value.getType().equals(type)){
				return value;
			}
		}
		return null;
	}
}
