package io.paddle.plugin.python.dependencies.parser.antlr;

// Generated from EnvMarkers.g4 by ANTLR 4.8

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class EnvMarkersParser extends Parser {
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
    public static final int
            RULE_markerOp = 0, RULE_envVar = 1, RULE_markerVar = 2, RULE_markerExpr = 3,
            RULE_markerAnd = 4, RULE_markerOr = 5, RULE_marker = 6, RULE_quotedMarker = 7;

    private static String[] makeRuleNames() {
        return new String[]{
                "markerOp", "envVar", "markerVar", "markerExpr", "markerAnd", "markerOr",
                "marker", "quotedMarker"
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
    public ATN getATN() {
        return _ATN;
    }

    public EnvMarkersParser(TokenStream input) {
        super(input);
        _interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
    }

    public static class MarkerOpContext extends ParserRuleContext {
        public TerminalNode VERSION_CMP() {
            return getToken(EnvMarkersParser.VERSION_CMP, 0);
        }

        public TerminalNode IN() {
            return getToken(EnvMarkersParser.IN, 0);
        }

        public TerminalNode NOT() {
            return getToken(EnvMarkersParser.NOT, 0);
        }

        public MarkerOpContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_markerOp;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof EnvMarkersListener) ((EnvMarkersListener) listener).enterMarkerOp(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof EnvMarkersListener) ((EnvMarkersListener) listener).exitMarkerOp(this);
        }
    }

    public final MarkerOpContext markerOp() throws RecognitionException {
        MarkerOpContext _localctx = new MarkerOpContext(_ctx, getState());
        enterRule(_localctx, 0, RULE_markerOp);
        try {
            setState(20);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case VERSION_CMP:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(16);
                    match(VERSION_CMP);
                }
                break;
                case IN:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(17);
                    match(IN);
                }
                break;
                case NOT:
                    enterOuterAlt(_localctx, 3);
                {
                    {
                        setState(18);
                        match(NOT);
                        setState(19);
                        match(IN);
                    }
                }
                break;
                default:
                    throw new NoViableAltException(this);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class EnvVarContext extends ParserRuleContext {
        public EnvVarContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_envVar;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof EnvMarkersListener) ((EnvMarkersListener) listener).enterEnvVar(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof EnvMarkersListener) ((EnvMarkersListener) listener).exitEnvVar(this);
        }
    }

    public final EnvVarContext envVar() throws RecognitionException {
        EnvVarContext _localctx = new EnvVarContext(_ctx, getState());
        enterRule(_localctx, 2, RULE_envVar);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(22);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << T__5) | (1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << T__10) | (1L << T__11))) != 0))) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class MarkerVarContext extends ParserRuleContext {
        public EnvVarContext envVar() {
            return getRuleContext(EnvVarContext.class, 0);
        }

        public TerminalNode PythonStr() {
            return getToken(EnvMarkersParser.PythonStr, 0);
        }

        public MarkerVarContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_markerVar;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof EnvMarkersListener) ((EnvMarkersListener) listener).enterMarkerVar(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof EnvMarkersListener) ((EnvMarkersListener) listener).exitMarkerVar(this);
        }
    }

    public final MarkerVarContext markerVar() throws RecognitionException {
        MarkerVarContext _localctx = new MarkerVarContext(_ctx, getState());
        enterRule(_localctx, 4, RULE_markerVar);
        try {
            setState(26);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case T__0:
                case T__1:
                case T__2:
                case T__3:
                case T__4:
                case T__5:
                case T__6:
                case T__7:
                case T__8:
                case T__9:
                case T__10:
                case T__11:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(24);
                    envVar();
                }
                break;
                case PythonStr:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(25);
                    match(PythonStr);
                }
                break;
                default:
                    throw new NoViableAltException(this);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class MarkerExprContext extends ParserRuleContext {
        public List<MarkerVarContext> markerVar() {
            return getRuleContexts(MarkerVarContext.class);
        }

        public MarkerVarContext markerVar(int i) {
            return getRuleContext(MarkerVarContext.class, i);
        }

        public MarkerOpContext markerOp() {
            return getRuleContext(MarkerOpContext.class, 0);
        }

        public MarkerContext marker() {
            return getRuleContext(MarkerContext.class, 0);
        }

        public MarkerExprContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_markerExpr;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof EnvMarkersListener) ((EnvMarkersListener) listener).enterMarkerExpr(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof EnvMarkersListener) ((EnvMarkersListener) listener).exitMarkerExpr(this);
        }
    }

    public final MarkerExprContext markerExpr() throws RecognitionException {
        MarkerExprContext _localctx = new MarkerExprContext(_ctx, getState());
        enterRule(_localctx, 6, RULE_markerExpr);
        try {
            setState(36);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case T__0:
                case T__1:
                case T__2:
                case T__3:
                case T__4:
                case T__5:
                case T__6:
                case T__7:
                case T__8:
                case T__9:
                case T__10:
                case T__11:
                case PythonStr:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(28);
                    markerVar();
                    setState(29);
                    markerOp();
                    setState(30);
                    markerVar();
                }
                break;
                case T__12:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(32);
                    match(T__12);
                    setState(33);
                    marker();
                    setState(34);
                    match(T__13);
                }
                break;
                default:
                    throw new NoViableAltException(this);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class MarkerAndContext extends ParserRuleContext {
        public List<MarkerExprContext> markerExpr() {
            return getRuleContexts(MarkerExprContext.class);
        }

        public MarkerExprContext markerExpr(int i) {
            return getRuleContext(MarkerExprContext.class, i);
        }

        public MarkerAndContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_markerAnd;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof EnvMarkersListener) ((EnvMarkersListener) listener).enterMarkerAnd(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof EnvMarkersListener) ((EnvMarkersListener) listener).exitMarkerAnd(this);
        }
    }

    public final MarkerAndContext markerAnd() throws RecognitionException {
        MarkerAndContext _localctx = new MarkerAndContext(_ctx, getState());
        enterRule(_localctx, 8, RULE_markerAnd);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(38);
                markerExpr();
                setState(41);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == T__14) {
                    {
                        setState(39);
                        match(T__14);
                        setState(40);
                        markerExpr();
                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class MarkerOrContext extends ParserRuleContext {
        public List<MarkerAndContext> markerAnd() {
            return getRuleContexts(MarkerAndContext.class);
        }

        public MarkerAndContext markerAnd(int i) {
            return getRuleContext(MarkerAndContext.class, i);
        }

        public MarkerOrContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_markerOr;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof EnvMarkersListener) ((EnvMarkersListener) listener).enterMarkerOr(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof EnvMarkersListener) ((EnvMarkersListener) listener).exitMarkerOr(this);
        }
    }

    public final MarkerOrContext markerOr() throws RecognitionException {
        MarkerOrContext _localctx = new MarkerOrContext(_ctx, getState());
        enterRule(_localctx, 10, RULE_markerOr);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(43);
                markerAnd();
                setState(46);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == T__15) {
                    {
                        setState(44);
                        match(T__15);
                        setState(45);
                        markerAnd();
                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class MarkerContext extends ParserRuleContext {
        public MarkerOrContext markerOr() {
            return getRuleContext(MarkerOrContext.class, 0);
        }

        public MarkerContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_marker;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof EnvMarkersListener) ((EnvMarkersListener) listener).enterMarker(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof EnvMarkersListener) ((EnvMarkersListener) listener).exitMarker(this);
        }
    }

    public final MarkerContext marker() throws RecognitionException {
        MarkerContext _localctx = new MarkerContext(_ctx, getState());
        enterRule(_localctx, 12, RULE_marker);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(48);
                markerOr();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class QuotedMarkerContext extends ParserRuleContext {
        public MarkerContext marker() {
            return getRuleContext(MarkerContext.class, 0);
        }

        public QuotedMarkerContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_quotedMarker;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof EnvMarkersListener) ((EnvMarkersListener) listener).enterQuotedMarker(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof EnvMarkersListener) ((EnvMarkersListener) listener).exitQuotedMarker(this);
        }
    }

    public final QuotedMarkerContext quotedMarker() throws RecognitionException {
        QuotedMarkerContext _localctx = new QuotedMarkerContext(_ctx, getState());
        enterRule(_localctx, 14, RULE_quotedMarker);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(50);
                match(T__16);
                setState(51);
                marker();
                setState(55);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == T__17) {
                    {
                        {
                            setState(52);
                            match(T__17);
                        }
                    }
                    setState(57);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static final String _serializedATN =
            "\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\36=\4\2\t\2\4\3\t" +
                    "\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\3\2\3\2\3\2\3\2\5\2" +
                    "\27\n\2\3\3\3\3\3\4\3\4\5\4\35\n\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\5\5" +
                    "\'\n\5\3\6\3\6\3\6\5\6,\n\6\3\7\3\7\3\7\5\7\61\n\7\3\b\3\b\3\t\3\t\3\t" +
                    "\7\t8\n\t\f\t\16\t;\13\t\3\t\2\2\n\2\4\6\b\n\f\16\20\2\3\3\2\3\16\2;\2" +
                    "\26\3\2\2\2\4\30\3\2\2\2\6\34\3\2\2\2\b&\3\2\2\2\n(\3\2\2\2\f-\3\2\2\2" +
                    "\16\62\3\2\2\2\20\64\3\2\2\2\22\27\7\26\2\2\23\27\7\36\2\2\24\25\7\35" +
                    "\2\2\25\27\7\36\2\2\26\22\3\2\2\2\26\23\3\2\2\2\26\24\3\2\2\2\27\3\3\2" +
                    "\2\2\30\31\t\2\2\2\31\5\3\2\2\2\32\35\5\4\3\2\33\35\7\32\2\2\34\32\3\2" +
                    "\2\2\34\33\3\2\2\2\35\7\3\2\2\2\36\37\5\6\4\2\37 \5\2\2\2 !\5\6\4\2!\'" +
                    "\3\2\2\2\"#\7\17\2\2#$\5\16\b\2$%\7\20\2\2%\'\3\2\2\2&\36\3\2\2\2&\"\3" +
                    "\2\2\2\'\t\3\2\2\2(+\5\b\5\2)*\7\21\2\2*,\5\b\5\2+)\3\2\2\2+,\3\2\2\2" +
                    ",\13\3\2\2\2-\60\5\n\6\2./\7\22\2\2/\61\5\n\6\2\60.\3\2\2\2\60\61\3\2" +
                    "\2\2\61\r\3\2\2\2\62\63\5\f\7\2\63\17\3\2\2\2\64\65\7\23\2\2\659\5\16" +
                    "\b\2\668\7\24\2\2\67\66\3\2\2\28;\3\2\2\29\67\3\2\2\29:\3\2\2\2:\21\3" +
                    "\2\2\2;9\3\2\2\2\b\26\34&+\609";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());

    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}
