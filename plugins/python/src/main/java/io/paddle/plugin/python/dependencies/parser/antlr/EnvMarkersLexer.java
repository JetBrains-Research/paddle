package io.paddle.plugin.python.dependencies.parser.antlr;

// Generated from EnvMarkers.g4 by ANTLR 4.8

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class EnvMarkersLexer extends Lexer {
    static {
        RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION);
    }

    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
            new PredictionContextCache();
    public static final int
            T__0 = 1, T__1 = 2, T__2 = 3, T__3 = 4, T__4 = 5, T__5 = 6, T__6 = 7, T__7 = 8, T__8 = 9,
            T__9 = 10, T__10 = 11, T__11 = 12, T__12 = 13, T__13 = 14, T__14 = 15, T__15 = 16, T__16 = 17,
            T__17 = 18, WS = 19, VERSION_CMP = 20, PYTHON_STR_C = 21, Dquote = 22, Squote = 23,
            PythonStr = 24, LETTER = 25, DIGIT = 26, NOT = 27, IN = 28;
    public static String[] channelNames = {
            "DEFAULT_TOKEN_CHANNEL", "HIDDEN"
    };

    public static String[] modeNames = {
            "DEFAULT_MODE"
    };

    private static String[] makeRuleNames() {
        return new String[]{
                "T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8",
                "T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "T__15", "T__16",
                "T__17", "WS", "VERSION_CMP", "PYTHON_STR_C", "Dquote", "Squote", "PythonStr",
                "LETTER", "DIGIT", "NOT", "IN"
        };
    }

    public static final String[] ruleNames = makeRuleNames();

    private static String[] makeLiteralNames() {
        return new String[]{
                null, "'python_version'", "'python_full_version'", "'os_name'", "'sys_platform'",
                "'platform_release'", "'platform_system'", "'platform_version'", "'platform_machine'",
                "'platform_python_implementation'", "'implementation_name'", "'implementation_version'",
                "'extra'", "'('", "')'", "'and'", "'or'", "';'", "'\n'", null, null,
                null, "'\"'", "'''", null, null, null, "'not'", "'in'"
        };
    }

    private static final String[] _LITERAL_NAMES = makeLiteralNames();

    private static String[] makeSymbolicNames() {
        return new String[]{
                null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, "WS", "VERSION_CMP", "PYTHON_STR_C",
                "Dquote", "Squote", "PythonStr", "LETTER", "DIGIT", "NOT", "IN"
        };
    }

    private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
    public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

    /**
     * @deprecated Use {@link #VOCABULARY} instead.
     */
    @Deprecated
    public static final String[] tokenNames;

    static {
        tokenNames = new String[_SYMBOLIC_NAMES.length];
        for (int i = 0; i < tokenNames.length; i++) {
            tokenNames[i] = VOCABULARY.getLiteralName(i);
            if (tokenNames[i] == null) {
                tokenNames[i] = VOCABULARY.getSymbolicName(i);
            }

            if (tokenNames[i] == null) {
                tokenNames[i] = "<INVALID>";
            }
        }
    }

    @Override
    @Deprecated
    public String[] getTokenNames() {
        return tokenNames;
    }

    @Override

    public Vocabulary getVocabulary() {
        return VOCABULARY;
    }


    public EnvMarkersLexer(CharStream input) {
        super(input);
        _interp = new LexerATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
    }

    @Override
    public String getGrammarFileName() {
        return "EnvMarkers.g4";
    }

    @Override
    public String[] getRuleNames() {
        return ruleNames;
    }

    @Override
    public String getSerializedATN() {
        return _serializedATN;
    }

    @Override
    public String[] getChannelNames() {
        return channelNames;
    }

    @Override
    public String[] getModeNames() {
        return modeNames;
    }

    @Override
    public ATN getATN() {
        return _ATN;
    }

    public static final String _serializedATN =
            "\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\36\u0158\b\1\4\2" +
                    "\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4" +
                    "\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22" +
                    "\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31" +
                    "\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\3\2\3\2\3\2\3\2\3\2\3\2" +
                    "\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3" +
                    "\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4" +
                    "\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3" +
                    "\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7" +
                    "\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3" +
                    "\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t" +
                    "\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3" +
                    "\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n" +
                    "\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3" +
                    "\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\f\3" +
                    "\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f" +
                    "\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20" +
                    "\3\20\3\20\3\21\3\21\3\21\3\22\3\22\3\23\3\23\3\24\6\24\u0117\n\24\r\24" +
                    "\16\24\u0118\3\24\3\24\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3" +
                    "\25\3\25\3\25\3\25\3\25\3\25\5\25\u012c\n\25\3\26\3\26\3\26\3\26\5\26" +
                    "\u0132\n\26\3\27\3\27\3\30\3\30\3\31\3\31\3\31\7\31\u013b\n\31\f\31\16" +
                    "\31\u013e\13\31\3\31\3\31\3\31\3\31\3\31\7\31\u0145\n\31\f\31\16\31\u0148" +
                    "\13\31\3\31\3\31\5\31\u014c\n\31\3\32\3\32\3\33\3\33\3\34\3\34\3\34\3" +
                    "\34\3\35\3\35\3\35\2\2\36\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25" +
                    "\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32" +
                    "\63\33\65\34\67\359\36\3\2\5\4\2\13\13\"\"\t\2##%(*\61<B]]_b}\u0080\4" +
                    "\2C\\c|\2\u0167\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3" +
                    "\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2" +
                    "\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3" +
                    "\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2" +
                    "\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\2" +
                    "9\3\2\2\2\3;\3\2\2\2\5J\3\2\2\2\7^\3\2\2\2\tf\3\2\2\2\13s\3\2\2\2\r\u0084" +
                    "\3\2\2\2\17\u0094\3\2\2\2\21\u00a5\3\2\2\2\23\u00b6\3\2\2\2\25\u00d5\3" +
                    "\2\2\2\27\u00e9\3\2\2\2\31\u0100\3\2\2\2\33\u0106\3\2\2\2\35\u0108\3\2" +
                    "\2\2\37\u010a\3\2\2\2!\u010e\3\2\2\2#\u0111\3\2\2\2%\u0113\3\2\2\2\'\u0116" +
                    "\3\2\2\2)\u012b\3\2\2\2+\u0131\3\2\2\2-\u0133\3\2\2\2/\u0135\3\2\2\2\61" +
                    "\u014b\3\2\2\2\63\u014d\3\2\2\2\65\u014f\3\2\2\2\67\u0151\3\2\2\29\u0155" +
                    "\3\2\2\2;<\7r\2\2<=\7{\2\2=>\7v\2\2>?\7j\2\2?@\7q\2\2@A\7p\2\2AB\7a\2" +
                    "\2BC\7x\2\2CD\7g\2\2DE\7t\2\2EF\7u\2\2FG\7k\2\2GH\7q\2\2HI\7p\2\2I\4\3" +
                    "\2\2\2JK\7r\2\2KL\7{\2\2LM\7v\2\2MN\7j\2\2NO\7q\2\2OP\7p\2\2PQ\7a\2\2" +
                    "QR\7h\2\2RS\7w\2\2ST\7n\2\2TU\7n\2\2UV\7a\2\2VW\7x\2\2WX\7g\2\2XY\7t\2" +
                    "\2YZ\7u\2\2Z[\7k\2\2[\\\7q\2\2\\]\7p\2\2]\6\3\2\2\2^_\7q\2\2_`\7u\2\2" +
                    "`a\7a\2\2ab\7p\2\2bc\7c\2\2cd\7o\2\2de\7g\2\2e\b\3\2\2\2fg\7u\2\2gh\7" +
                    "{\2\2hi\7u\2\2ij\7a\2\2jk\7r\2\2kl\7n\2\2lm\7c\2\2mn\7v\2\2no\7h\2\2o" +
                    "p\7q\2\2pq\7t\2\2qr\7o\2\2r\n\3\2\2\2st\7r\2\2tu\7n\2\2uv\7c\2\2vw\7v" +
                    "\2\2wx\7h\2\2xy\7q\2\2yz\7t\2\2z{\7o\2\2{|\7a\2\2|}\7t\2\2}~\7g\2\2~\177" +
                    "\7n\2\2\177\u0080\7g\2\2\u0080\u0081\7c\2\2\u0081\u0082\7u\2\2\u0082\u0083" +
                    "\7g\2\2\u0083\f\3\2\2\2\u0084\u0085\7r\2\2\u0085\u0086\7n\2\2\u0086\u0087" +
                    "\7c\2\2\u0087\u0088\7v\2\2\u0088\u0089\7h\2\2\u0089\u008a\7q\2\2\u008a" +
                    "\u008b\7t\2\2\u008b\u008c\7o\2\2\u008c\u008d\7a\2\2\u008d\u008e\7u\2\2" +
                    "\u008e\u008f\7{\2\2\u008f\u0090\7u\2\2\u0090\u0091\7v\2\2\u0091\u0092" +
                    "\7g\2\2\u0092\u0093\7o\2\2\u0093\16\3\2\2\2\u0094\u0095\7r\2\2\u0095\u0096" +
                    "\7n\2\2\u0096\u0097\7c\2\2\u0097\u0098\7v\2\2\u0098\u0099\7h\2\2\u0099" +
                    "\u009a\7q\2\2\u009a\u009b\7t\2\2\u009b\u009c\7o\2\2\u009c\u009d\7a\2\2" +
                    "\u009d\u009e\7x\2\2\u009e\u009f\7g\2\2\u009f\u00a0\7t\2\2\u00a0\u00a1" +
                    "\7u\2\2\u00a1\u00a2\7k\2\2\u00a2\u00a3\7q\2\2\u00a3\u00a4\7p\2\2\u00a4" +
                    "\20\3\2\2\2\u00a5\u00a6\7r\2\2\u00a6\u00a7\7n\2\2\u00a7\u00a8\7c\2\2\u00a8" +
                    "\u00a9\7v\2\2\u00a9\u00aa\7h\2\2\u00aa\u00ab\7q\2\2\u00ab\u00ac\7t\2\2" +
                    "\u00ac\u00ad\7o\2\2\u00ad\u00ae\7a\2\2\u00ae\u00af\7o\2\2\u00af\u00b0" +
                    "\7c\2\2\u00b0\u00b1\7e\2\2\u00b1\u00b2\7j\2\2\u00b2\u00b3\7k\2\2\u00b3" +
                    "\u00b4\7p\2\2\u00b4\u00b5\7g\2\2\u00b5\22\3\2\2\2\u00b6\u00b7\7r\2\2\u00b7" +
                    "\u00b8\7n\2\2\u00b8\u00b9\7c\2\2\u00b9\u00ba\7v\2\2\u00ba\u00bb\7h\2\2" +
                    "\u00bb\u00bc\7q\2\2\u00bc\u00bd\7t\2\2\u00bd\u00be\7o\2\2\u00be\u00bf" +
                    "\7a\2\2\u00bf\u00c0\7r\2\2\u00c0\u00c1\7{\2\2\u00c1\u00c2\7v\2\2\u00c2" +
                    "\u00c3\7j\2\2\u00c3\u00c4\7q\2\2\u00c4\u00c5\7p\2\2\u00c5\u00c6\7a\2\2" +
                    "\u00c6\u00c7\7k\2\2\u00c7\u00c8\7o\2\2\u00c8\u00c9\7r\2\2\u00c9\u00ca" +
                    "\7n\2\2\u00ca\u00cb\7g\2\2\u00cb\u00cc\7o\2\2\u00cc\u00cd\7g\2\2\u00cd" +
                    "\u00ce\7p\2\2\u00ce\u00cf\7v\2\2\u00cf\u00d0\7c\2\2\u00d0\u00d1\7v\2\2" +
                    "\u00d1\u00d2\7k\2\2\u00d2\u00d3\7q\2\2\u00d3\u00d4\7p\2\2\u00d4\24\3\2" +
                    "\2\2\u00d5\u00d6\7k\2\2\u00d6\u00d7\7o\2\2\u00d7\u00d8\7r\2\2\u00d8\u00d9" +
                    "\7n\2\2\u00d9\u00da\7g\2\2\u00da\u00db\7o\2\2\u00db\u00dc\7g\2\2\u00dc" +
                    "\u00dd\7p\2\2\u00dd\u00de\7v\2\2\u00de\u00df\7c\2\2\u00df\u00e0\7v\2\2" +
                    "\u00e0\u00e1\7k\2\2\u00e1\u00e2\7q\2\2\u00e2\u00e3\7p\2\2\u00e3\u00e4" +
                    "\7a\2\2\u00e4\u00e5\7p\2\2\u00e5\u00e6\7c\2\2\u00e6\u00e7\7o\2\2\u00e7" +
                    "\u00e8\7g\2\2\u00e8\26\3\2\2\2\u00e9\u00ea\7k\2\2\u00ea\u00eb\7o\2\2\u00eb" +
                    "\u00ec\7r\2\2\u00ec\u00ed\7n\2\2\u00ed\u00ee\7g\2\2\u00ee\u00ef\7o\2\2" +
                    "\u00ef\u00f0\7g\2\2\u00f0\u00f1\7p\2\2\u00f1\u00f2\7v\2\2\u00f2\u00f3" +
                    "\7c\2\2\u00f3\u00f4\7v\2\2\u00f4\u00f5\7k\2\2\u00f5\u00f6\7q\2\2\u00f6" +
                    "\u00f7\7p\2\2\u00f7\u00f8\7a\2\2\u00f8\u00f9\7x\2\2\u00f9\u00fa\7g\2\2" +
                    "\u00fa\u00fb\7t\2\2\u00fb\u00fc\7u\2\2\u00fc\u00fd\7k\2\2\u00fd\u00fe" +
                    "\7q\2\2\u00fe\u00ff\7p\2\2\u00ff\30\3\2\2\2\u0100\u0101\7g\2\2\u0101\u0102" +
                    "\7z\2\2\u0102\u0103\7v\2\2\u0103\u0104\7t\2\2\u0104\u0105\7c\2\2\u0105" +
                    "\32\3\2\2\2\u0106\u0107\7*\2\2\u0107\34\3\2\2\2\u0108\u0109\7+\2\2\u0109" +
                    "\36\3\2\2\2\u010a\u010b\7c\2\2\u010b\u010c\7p\2\2\u010c\u010d\7f\2\2\u010d" +
                    " \3\2\2\2\u010e\u010f\7q\2\2\u010f\u0110\7t\2\2\u0110\"\3\2\2\2\u0111" +
                    "\u0112\7=\2\2\u0112$\3\2\2\2\u0113\u0114\7\f\2\2\u0114&\3\2\2\2\u0115" +
                    "\u0117\t\2\2\2\u0116\u0115\3\2\2\2\u0117\u0118\3\2\2\2\u0118\u0116\3\2" +
                    "\2\2\u0118\u0119\3\2\2\2\u0119\u011a\3\2\2\2\u011a\u011b\b\24\2\2\u011b" +
                    "(\3\2\2\2\u011c\u012c\7>\2\2\u011d\u011e\7>\2\2\u011e\u012c\7?\2\2\u011f" +
                    "\u0120\7#\2\2\u0120\u012c\7?\2\2\u0121\u0122\7?\2\2\u0122\u012c\7?\2\2" +
                    "\u0123\u0124\7@\2\2\u0124\u012c\7?\2\2\u0125\u012c\7@\2\2\u0126\u0127" +
                    "\7\u0080\2\2\u0127\u012c\7?\2\2\u0128\u0129\7?\2\2\u0129\u012a\7?\2\2" +
                    "\u012a\u012c\7?\2\2\u012b\u011c\3\2\2\2\u012b\u011d\3\2\2\2\u012b\u011f" +
                    "\3\2\2\2\u012b\u0121\3\2\2\2\u012b\u0123\3\2\2\2\u012b\u0125\3\2\2\2\u012b" +
                    "\u0126\3\2\2\2\u012b\u0128\3\2\2\2\u012c*\3\2\2\2\u012d\u0132\5\'\24\2" +
                    "\u012e\u0132\5\63\32\2\u012f\u0132\5\65\33\2\u0130\u0132\t\3\2\2\u0131" +
                    "\u012d\3\2\2\2\u0131\u012e\3\2\2\2\u0131\u012f\3\2\2\2\u0131\u0130\3\2" +
                    "\2\2\u0132,\3\2\2\2\u0133\u0134\7$\2\2\u0134.\3\2\2\2\u0135\u0136\7)\2" +
                    "\2\u0136\60\3\2\2\2\u0137\u013c\5/\30\2\u0138\u013b\5+\26\2\u0139\u013b" +
                    "\5-\27\2\u013a\u0138\3\2\2\2\u013a\u0139\3\2\2\2\u013b\u013e\3\2\2\2\u013c" +
                    "\u013a\3\2\2\2\u013c\u013d\3\2\2\2\u013d\u013f\3\2\2\2\u013e\u013c\3\2" +
                    "\2\2\u013f\u0140\5/\30\2\u0140\u014c\3\2\2\2\u0141\u0146\5-\27\2\u0142" +
                    "\u0145\5+\26\2\u0143\u0145\5/\30\2\u0144\u0142\3\2\2\2\u0144\u0143\3\2" +
                    "\2\2\u0145\u0148\3\2\2\2\u0146\u0144\3\2\2\2\u0146\u0147\3\2\2\2\u0147" +
                    "\u0149\3\2\2\2\u0148\u0146\3\2\2\2\u0149\u014a\5-\27\2\u014a\u014c\3\2" +
                    "\2\2\u014b\u0137\3\2\2\2\u014b\u0141\3\2\2\2\u014c\62\3\2\2\2\u014d\u014e" +
                    "\t\4\2\2\u014e\64\3\2\2\2\u014f\u0150\4\62;\2\u0150\66\3\2\2\2\u0151\u0152" +
                    "\7p\2\2\u0152\u0153\7q\2\2\u0153\u0154\7v\2\2\u01548\3\2\2\2\u0155\u0156" +
                    "\7k\2\2\u0156\u0157\7p\2\2\u0157:\3\2\2\2\13\2\u0118\u012b\u0131\u013a" +
                    "\u013c\u0144\u0146\u014b\3\b\2\2";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());

    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}
