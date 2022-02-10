package io.paddle.plugin.python.dependencies.parser.antlr;

// Generated from DependencySpecification.g4 by ANTLR 4.8

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
public class DependencySpecificationParser extends Parser {
    static {
        RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION);
    }

    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
            new PredictionContextCache();
    public static final int
            T__0 = 1, T__1 = 2, T__2 = 3, T__3 = 4, T__4 = 5, T__5 = 6, T__6 = 7, T__7 = 8, T__8 = 9,
            T__9 = 10, T__10 = 11, T__11 = 12, T__12 = 13, T__13 = 14, T__14 = 15, T__15 = 16, T__16 = 17,
            WSP = 18, LETTER = 19, DIGIT = 20, ENV_VAR = 21, VERSION_CMP = 22, IDENT_END = 23,
            IDENT = 24, PYTHON_STR_C = 25, DQUOTE = 26, SQUOTE = 27, PYTHON_STR = 28;
    public static final int
            RULE_version = 0, RULE_versionCmp = 1, RULE_versionOne = 2, RULE_versionMany = 3,
            RULE_versionspec = 4, RULE_markerOp = 5, RULE_markerVar = 6, RULE_markerExpr = 7,
            RULE_markerAnd = 8, RULE_markerOr = 9, RULE_marker = 10, RULE_quotedMarker = 11,
            RULE_name = 12, RULE_extrasList = 13, RULE_extras = 14, RULE_nameReq = 15,
            RULE_specification = 16;

    private static String[] makeRuleNames() {
        return new String[]{
                "version", "versionCmp", "versionOne", "versionMany", "versionspec",
                "markerOp", "markerVar", "markerExpr", "markerAnd", "markerOr", "marker",
                "quotedMarker", "name", "extrasList", "extras", "nameReq", "specification"
        };
    }

    public static final String[] ruleNames = makeRuleNames();

    private static String[] makeLiteralNames() {
        return new String[]{
                null, "'-'", "'_'", "'.'", "'*'", "'+'", "'!'", "','", "'('", "')'",
                "'in'", "'not'", "'and'", "'or'", "';'", "'['", "']'", "'\n'", null,
                null, null, null, null, null, null, null, "'\"'", "'''"
        };
    }

    private static final String[] _LITERAL_NAMES = makeLiteralNames();

    private static String[] makeSymbolicNames() {
        return new String[]{
                null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, "WSP", "LETTER", "DIGIT", "ENV_VAR",
                "VERSION_CMP", "IDENT_END", "IDENT", "PYTHON_STR_C", "DQUOTE", "SQUOTE",
                "PYTHON_STR"
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
        return "DependencySpecification.g4";
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

    public DependencySpecificationParser(TokenStream input) {
        super(input);
        _interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
    }

    public static class VersionContext extends ParserRuleContext {
        public List<TerminalNode> WSP() {
            return getTokens(DependencySpecificationParser.WSP);
        }

        public TerminalNode WSP(int i) {
            return getToken(DependencySpecificationParser.WSP, i);
        }

        public List<TerminalNode> IDENT() {
            return getTokens(DependencySpecificationParser.IDENT);
        }

        public TerminalNode IDENT(int i) {
            return getToken(DependencySpecificationParser.IDENT, i);
        }

        public List<TerminalNode> LETTER() {
            return getTokens(DependencySpecificationParser.LETTER);
        }

        public TerminalNode LETTER(int i) {
            return getToken(DependencySpecificationParser.LETTER, i);
        }

        public List<TerminalNode> DIGIT() {
            return getTokens(DependencySpecificationParser.DIGIT);
        }

        public TerminalNode DIGIT(int i) {
            return getToken(DependencySpecificationParser.DIGIT, i);
        }

        public VersionContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_version;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).enterVersion(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).exitVersion(this);
        }
    }

    public final VersionContext version() throws RecognitionException {
        VersionContext _localctx = new VersionContext(_ctx, getState());
        enterRule(_localctx, 0, RULE_version);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(37);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == WSP) {
                    {
                        {
                            setState(34);
                            match(WSP);
                        }
                    }
                    setState(39);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(41);
                _errHandler.sync(this);
                _la = _input.LA(1);
                do {
                    {
                        {
                            setState(40);
                            _la = _input.LA(1);
                            if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << T__5) | (1L << LETTER) | (1L << DIGIT) | (1L << IDENT))) != 0))) {
                                _errHandler.recoverInline(this);
                            } else {
                                if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                _errHandler.reportMatch(this);
                                consume();
                            }
                        }
                    }
                    setState(43);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                } while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << T__5) | (1L << LETTER) | (1L << DIGIT) | (1L << IDENT))) != 0));
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

    public static class VersionCmpContext extends ParserRuleContext {
        public TerminalNode VERSION_CMP() {
            return getToken(DependencySpecificationParser.VERSION_CMP, 0);
        }

        public VersionCmpContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_versionCmp;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).enterVersionCmp(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).exitVersionCmp(this);
        }
    }

    public final VersionCmpContext versionCmp() throws RecognitionException {
        VersionCmpContext _localctx = new VersionCmpContext(_ctx, getState());
        enterRule(_localctx, 2, RULE_versionCmp);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(45);
                match(VERSION_CMP);
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

    public static class VersionOneContext extends ParserRuleContext {
        public VersionCmpContext versionCmp() {
            return getRuleContext(VersionCmpContext.class, 0);
        }

        public VersionContext version() {
            return getRuleContext(VersionContext.class, 0);
        }

        public List<TerminalNode> WSP() {
            return getTokens(DependencySpecificationParser.WSP);
        }

        public TerminalNode WSP(int i) {
            return getToken(DependencySpecificationParser.WSP, i);
        }

        public VersionOneContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_versionOne;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).enterVersionOne(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).exitVersionOne(this);
        }
    }

    public final VersionOneContext versionOne() throws RecognitionException {
        VersionOneContext _localctx = new VersionOneContext(_ctx, getState());
        enterRule(_localctx, 4, RULE_versionOne);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(47);
                versionCmp();
                setState(48);
                version();
                setState(52);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 2, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(49);
                                match(WSP);
                            }
                        }
                    }
                    setState(54);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 2, _ctx);
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

    public static class VersionManyContext extends ParserRuleContext {
        public List<VersionOneContext> versionOne() {
            return getRuleContexts(VersionOneContext.class);
        }

        public VersionOneContext versionOne(int i) {
            return getRuleContext(VersionOneContext.class, i);
        }

        public List<TerminalNode> WSP() {
            return getTokens(DependencySpecificationParser.WSP);
        }

        public TerminalNode WSP(int i) {
            return getToken(DependencySpecificationParser.WSP, i);
        }

        public VersionManyContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_versionMany;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).enterVersionMany(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).exitVersionMany(this);
        }
    }

    public final VersionManyContext versionMany() throws RecognitionException {
        VersionManyContext _localctx = new VersionManyContext(_ctx, getState());
        enterRule(_localctx, 6, RULE_versionMany);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(55);
                versionOne();
                setState(66);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 4, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(59);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                while (_la == WSP) {
                                    {
                                        {
                                            setState(56);
                                            match(WSP);
                                        }
                                    }
                                    setState(61);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                }
                                setState(62);
                                match(T__6);
                                setState(63);
                                versionOne();
                            }
                        }
                    }
                    setState(68);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 4, _ctx);
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

    public static class VersionspecContext extends ParserRuleContext {
        public VersionManyContext versionMany() {
            return getRuleContext(VersionManyContext.class, 0);
        }

        public VersionspecContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_versionspec;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).enterVersionspec(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).exitVersionspec(this);
        }
    }

    public final VersionspecContext versionspec() throws RecognitionException {
        VersionspecContext _localctx = new VersionspecContext(_ctx, getState());
        enterRule(_localctx, 8, RULE_versionspec);
        try {
            setState(74);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case T__7:
                    enterOuterAlt(_localctx, 1);
                {
                    {
                        setState(69);
                        match(T__7);
                        setState(70);
                        versionMany();
                        setState(71);
                        match(T__8);
                    }
                }
                break;
                case VERSION_CMP:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(73);
                    versionMany();
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

    public static class MarkerOpContext extends ParserRuleContext {
        public TerminalNode VERSION_CMP() {
            return getToken(DependencySpecificationParser.VERSION_CMP, 0);
        }

        public List<TerminalNode> WSP() {
            return getTokens(DependencySpecificationParser.WSP);
        }

        public TerminalNode WSP(int i) {
            return getToken(DependencySpecificationParser.WSP, i);
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
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).enterMarkerOp(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).exitMarkerOp(this);
        }
    }

    public final MarkerOpContext markerOp() throws RecognitionException {
        MarkerOpContext _localctx = new MarkerOpContext(_ctx, getState());
        enterRule(_localctx, 10, RULE_markerOp);
        int _la;
        try {
            int _alt;
            setState(105);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 11, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(76);
                    match(VERSION_CMP);
                }
                break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                {
                    {
                        setState(78);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        do {
                            {
                                {
                                    setState(77);
                                    match(WSP);
                                }
                            }
                            setState(80);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        } while (_la == WSP);
                        setState(82);
                        match(T__9);
                        setState(84);
                        _errHandler.sync(this);
                        _alt = 1;
                        do {
                            switch (_alt) {
                                case 1: {
                                    {
                                        setState(83);
                                        match(WSP);
                                    }
                                }
                                break;
                                default:
                                    throw new NoViableAltException(this);
                            }
                            setState(86);
                            _errHandler.sync(this);
                            _alt = getInterpreter().adaptivePredict(_input, 7, _ctx);
                        } while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER);
                    }
                }
                break;
                case 3:
                    enterOuterAlt(_localctx, 3);
                {
                    {
                        setState(89);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        do {
                            {
                                {
                                    setState(88);
                                    match(WSP);
                                }
                            }
                            setState(91);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        } while (_la == WSP);
                        setState(93);
                        match(T__10);
                        setState(95);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        do {
                            {
                                {
                                    setState(94);
                                    match(WSP);
                                }
                            }
                            setState(97);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        } while (_la == WSP);
                        setState(99);
                        match(T__9);
                        setState(101);
                        _errHandler.sync(this);
                        _alt = 1;
                        do {
                            switch (_alt) {
                                case 1: {
                                    {
                                        setState(100);
                                        match(WSP);
                                    }
                                }
                                break;
                                default:
                                    throw new NoViableAltException(this);
                            }
                            setState(103);
                            _errHandler.sync(this);
                            _alt = getInterpreter().adaptivePredict(_input, 10, _ctx);
                        } while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER);
                    }
                }
                break;
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
        public TerminalNode ENV_VAR() {
            return getToken(DependencySpecificationParser.ENV_VAR, 0);
        }

        public TerminalNode PYTHON_STR() {
            return getToken(DependencySpecificationParser.PYTHON_STR, 0);
        }

        public List<TerminalNode> WSP() {
            return getTokens(DependencySpecificationParser.WSP);
        }

        public TerminalNode WSP(int i) {
            return getToken(DependencySpecificationParser.WSP, i);
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
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).enterMarkerVar(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).exitMarkerVar(this);
        }
    }

    public final MarkerVarContext markerVar() throws RecognitionException {
        MarkerVarContext _localctx = new MarkerVarContext(_ctx, getState());
        enterRule(_localctx, 12, RULE_markerVar);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(110);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == WSP) {
                    {
                        {
                            setState(107);
                            match(WSP);
                        }
                    }
                    setState(112);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(113);
                _la = _input.LA(1);
                if (!(_la == ENV_VAR || _la == PYTHON_STR)) {
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

        public List<TerminalNode> WSP() {
            return getTokens(DependencySpecificationParser.WSP);
        }

        public TerminalNode WSP(int i) {
            return getToken(DependencySpecificationParser.WSP, i);
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
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).enterMarkerExpr(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).exitMarkerExpr(this);
        }
    }

    public final MarkerExprContext markerExpr() throws RecognitionException {
        MarkerExprContext _localctx = new MarkerExprContext(_ctx, getState());
        enterRule(_localctx, 14, RULE_markerExpr);
        int _la;
        try {
            setState(135);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 15, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(115);
                    markerVar();
                    setState(116);
                    markerOp();
                    setState(117);
                    markerVar();
                }
                break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(122);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == WSP) {
                        {
                            {
                                setState(119);
                                match(WSP);
                            }
                        }
                        setState(124);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                    setState(125);
                    match(T__7);
                    setState(126);
                    marker();
                    setState(130);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == WSP) {
                        {
                            {
                                setState(127);
                                match(WSP);
                            }
                        }
                        setState(132);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                    setState(133);
                    match(T__8);
                }
                break;
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

        public List<TerminalNode> WSP() {
            return getTokens(DependencySpecificationParser.WSP);
        }

        public TerminalNode WSP(int i) {
            return getToken(DependencySpecificationParser.WSP, i);
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
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).enterMarkerAnd(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).exitMarkerAnd(this);
        }
    }

    public final MarkerAndContext markerAnd() throws RecognitionException {
        MarkerAndContext _localctx = new MarkerAndContext(_ctx, getState());
        enterRule(_localctx, 16, RULE_markerAnd);
        int _la;
        try {
            setState(148);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 17, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(137);
                    markerExpr();
                    setState(141);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == WSP) {
                        {
                            {
                                setState(138);
                                match(WSP);
                            }
                        }
                        setState(143);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                    setState(144);
                    match(T__11);
                    setState(145);
                    markerExpr();
                }
                break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(147);
                    markerExpr();
                }
                break;
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

        public List<TerminalNode> WSP() {
            return getTokens(DependencySpecificationParser.WSP);
        }

        public TerminalNode WSP(int i) {
            return getToken(DependencySpecificationParser.WSP, i);
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
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).enterMarkerOr(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).exitMarkerOr(this);
        }
    }

    public final MarkerOrContext markerOr() throws RecognitionException {
        MarkerOrContext _localctx = new MarkerOrContext(_ctx, getState());
        enterRule(_localctx, 18, RULE_markerOr);
        int _la;
        try {
            setState(161);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 19, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(150);
                    markerAnd();
                    setState(154);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == WSP) {
                        {
                            {
                                setState(151);
                                match(WSP);
                            }
                        }
                        setState(156);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                    setState(157);
                    match(T__12);
                    setState(158);
                    markerAnd();
                }
                break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(160);
                    markerAnd();
                }
                break;
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
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).enterMarker(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).exitMarker(this);
        }
    }

    public final MarkerContext marker() throws RecognitionException {
        MarkerContext _localctx = new MarkerContext(_ctx, getState());
        enterRule(_localctx, 20, RULE_marker);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(163);
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

        public List<TerminalNode> WSP() {
            return getTokens(DependencySpecificationParser.WSP);
        }

        public TerminalNode WSP(int i) {
            return getToken(DependencySpecificationParser.WSP, i);
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
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).enterQuotedMarker(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).exitQuotedMarker(this);
        }
    }

    public final QuotedMarkerContext quotedMarker() throws RecognitionException {
        QuotedMarkerContext _localctx = new QuotedMarkerContext(_ctx, getState());
        enterRule(_localctx, 22, RULE_quotedMarker);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(165);
                match(T__13);
                setState(169);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 20, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(166);
                                match(WSP);
                            }
                        }
                    }
                    setState(171);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 20, _ctx);
                }
                setState(172);
                marker();
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

    public static class NameContext extends ParserRuleContext {
        public TerminalNode IDENT() {
            return getToken(DependencySpecificationParser.IDENT, 0);
        }

        public NameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).enterName(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).exitName(this);
        }
    }

    public final NameContext name() throws RecognitionException {
        NameContext _localctx = new NameContext(_ctx, getState());
        enterRule(_localctx, 24, RULE_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(174);
                match(IDENT);
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

    public static class ExtrasListContext extends ParserRuleContext {
        public List<NameContext> name() {
            return getRuleContexts(NameContext.class);
        }

        public NameContext name(int i) {
            return getRuleContext(NameContext.class, i);
        }

        public List<TerminalNode> WSP() {
            return getTokens(DependencySpecificationParser.WSP);
        }

        public TerminalNode WSP(int i) {
            return getToken(DependencySpecificationParser.WSP, i);
        }

        public ExtrasListContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_extrasList;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).enterExtrasList(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).exitExtrasList(this);
        }
    }

    public final ExtrasListContext extrasList() throws RecognitionException {
        ExtrasListContext _localctx = new ExtrasListContext(_ctx, getState());
        enterRule(_localctx, 26, RULE_extrasList);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(176);
                name();
                setState(193);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 23, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(180);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                while (_la == WSP) {
                                    {
                                        {
                                            setState(177);
                                            match(WSP);
                                        }
                                    }
                                    setState(182);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                }
                                setState(183);
                                match(T__6);
                                setState(187);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                while (_la == WSP) {
                                    {
                                        {
                                            setState(184);
                                            match(WSP);
                                        }
                                    }
                                    setState(189);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                }
                                setState(190);
                                name();
                            }
                        }
                    }
                    setState(195);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 23, _ctx);
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

    public static class ExtrasContext extends ParserRuleContext {
        public List<TerminalNode> WSP() {
            return getTokens(DependencySpecificationParser.WSP);
        }

        public TerminalNode WSP(int i) {
            return getToken(DependencySpecificationParser.WSP, i);
        }

        public ExtrasListContext extrasList() {
            return getRuleContext(ExtrasListContext.class, 0);
        }

        public ExtrasContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_extras;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).enterExtras(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).exitExtras(this);
        }
    }

    public final ExtrasContext extras() throws RecognitionException {
        ExtrasContext _localctx = new ExtrasContext(_ctx, getState());
        enterRule(_localctx, 28, RULE_extras);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(196);
                match(T__14);
                setState(200);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 24, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(197);
                                match(WSP);
                            }
                        }
                    }
                    setState(202);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 24, _ctx);
                }
                setState(204);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == IDENT) {
                    {
                        setState(203);
                        extrasList();
                    }
                }

                setState(209);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == WSP) {
                    {
                        {
                            setState(206);
                            match(WSP);
                        }
                    }
                    setState(211);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(212);
                match(T__15);
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

    public static class NameReqContext extends ParserRuleContext {
        public NameContext name() {
            return getRuleContext(NameContext.class, 0);
        }

        public List<TerminalNode> WSP() {
            return getTokens(DependencySpecificationParser.WSP);
        }

        public TerminalNode WSP(int i) {
            return getToken(DependencySpecificationParser.WSP, i);
        }

        public ExtrasContext extras() {
            return getRuleContext(ExtrasContext.class, 0);
        }

        public VersionspecContext versionspec() {
            return getRuleContext(VersionspecContext.class, 0);
        }

        public QuotedMarkerContext quotedMarker() {
            return getRuleContext(QuotedMarkerContext.class, 0);
        }

        public NameReqContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_nameReq;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).enterNameReq(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).exitNameReq(this);
        }
    }

    public final NameReqContext nameReq() throws RecognitionException {
        NameReqContext _localctx = new NameReqContext(_ctx, getState());
        enterRule(_localctx, 30, RULE_nameReq);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(214);
                name();
                setState(218);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 27, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(215);
                                match(WSP);
                            }
                        }
                    }
                    setState(220);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 27, _ctx);
                }
                setState(222);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == T__14) {
                    {
                        setState(221);
                        extras();
                    }
                }

                setState(227);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 29, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(224);
                                match(WSP);
                            }
                        }
                    }
                    setState(229);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 29, _ctx);
                }
                setState(231);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == T__7 || _la == VERSION_CMP) {
                    {
                        setState(230);
                        versionspec();
                    }
                }

                setState(236);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 31, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(233);
                                match(WSP);
                            }
                        }
                    }
                    setState(238);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 31, _ctx);
                }
                setState(240);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == T__13) {
                    {
                        setState(239);
                        quotedMarker();
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

    public static class SpecificationContext extends ParserRuleContext {
        public NameReqContext nameReq() {
            return getRuleContext(NameReqContext.class, 0);
        }

        public List<TerminalNode> WSP() {
            return getTokens(DependencySpecificationParser.WSP);
        }

        public TerminalNode WSP(int i) {
            return getToken(DependencySpecificationParser.WSP, i);
        }

        public SpecificationContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_specification;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).enterSpecification(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof DependencySpecificationListener) ((DependencySpecificationListener) listener).exitSpecification(this);
        }
    }

    public final SpecificationContext specification() throws RecognitionException {
        SpecificationContext _localctx = new SpecificationContext(_ctx, getState());
        enterRule(_localctx, 32, RULE_specification);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(245);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == WSP) {
                    {
                        {
                            setState(242);
                            match(WSP);
                        }
                    }
                    setState(247);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                {
                    setState(248);
                    nameReq();
                }
                setState(252);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == WSP) {
                    {
                        {
                            setState(249);
                            match(WSP);
                        }
                    }
                    setState(254);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(258);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == T__16) {
                    {
                        {
                            setState(255);
                            match(T__16);
                        }
                    }
                    setState(260);
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
            "\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\36\u0108\4\2\t\2" +
                    "\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13" +
                    "\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22" +
                    "\3\2\7\2&\n\2\f\2\16\2)\13\2\3\2\6\2,\n\2\r\2\16\2-\3\3\3\3\3\4\3\4\3" +
                    "\4\7\4\65\n\4\f\4\16\48\13\4\3\5\3\5\7\5<\n\5\f\5\16\5?\13\5\3\5\3\5\7" +
                    "\5C\n\5\f\5\16\5F\13\5\3\6\3\6\3\6\3\6\3\6\5\6M\n\6\3\7\3\7\6\7Q\n\7\r" +
                    "\7\16\7R\3\7\3\7\6\7W\n\7\r\7\16\7X\3\7\6\7\\\n\7\r\7\16\7]\3\7\3\7\6" +
                    "\7b\n\7\r\7\16\7c\3\7\3\7\6\7h\n\7\r\7\16\7i\5\7l\n\7\3\b\7\bo\n\b\f\b" +
                    "\16\br\13\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\7\t{\n\t\f\t\16\t~\13\t\3\t\3" +
                    "\t\3\t\7\t\u0083\n\t\f\t\16\t\u0086\13\t\3\t\3\t\5\t\u008a\n\t\3\n\3\n" +
                    "\7\n\u008e\n\n\f\n\16\n\u0091\13\n\3\n\3\n\3\n\3\n\5\n\u0097\n\n\3\13" +
                    "\3\13\7\13\u009b\n\13\f\13\16\13\u009e\13\13\3\13\3\13\3\13\3\13\5\13" +
                    "\u00a4\n\13\3\f\3\f\3\r\3\r\7\r\u00aa\n\r\f\r\16\r\u00ad\13\r\3\r\3\r" +
                    "\3\16\3\16\3\17\3\17\7\17\u00b5\n\17\f\17\16\17\u00b8\13\17\3\17\3\17" +
                    "\7\17\u00bc\n\17\f\17\16\17\u00bf\13\17\3\17\7\17\u00c2\n\17\f\17\16\17" +
                    "\u00c5\13\17\3\20\3\20\7\20\u00c9\n\20\f\20\16\20\u00cc\13\20\3\20\5\20" +
                    "\u00cf\n\20\3\20\7\20\u00d2\n\20\f\20\16\20\u00d5\13\20\3\20\3\20\3\21" +
                    "\3\21\7\21\u00db\n\21\f\21\16\21\u00de\13\21\3\21\5\21\u00e1\n\21\3\21" +
                    "\7\21\u00e4\n\21\f\21\16\21\u00e7\13\21\3\21\5\21\u00ea\n\21\3\21\7\21" +
                    "\u00ed\n\21\f\21\16\21\u00f0\13\21\3\21\5\21\u00f3\n\21\3\22\7\22\u00f6" +
                    "\n\22\f\22\16\22\u00f9\13\22\3\22\3\22\7\22\u00fd\n\22\f\22\16\22\u0100" +
                    "\13\22\3\22\7\22\u0103\n\22\f\22\16\22\u0106\13\22\3\22\2\2\23\2\4\6\b" +
                    "\n\f\16\20\22\24\26\30\32\34\36 \"\2\4\5\2\3\b\25\26\32\32\4\2\27\27\36" +
                    "\36\2\u011b\2\'\3\2\2\2\4/\3\2\2\2\6\61\3\2\2\2\b9\3\2\2\2\nL\3\2\2\2" +
                    "\fk\3\2\2\2\16p\3\2\2\2\20\u0089\3\2\2\2\22\u0096\3\2\2\2\24\u00a3\3\2" +
                    "\2\2\26\u00a5\3\2\2\2\30\u00a7\3\2\2\2\32\u00b0\3\2\2\2\34\u00b2\3\2\2" +
                    "\2\36\u00c6\3\2\2\2 \u00d8\3\2\2\2\"\u00f7\3\2\2\2$&\7\24\2\2%$\3\2\2" +
                    "\2&)\3\2\2\2\'%\3\2\2\2\'(\3\2\2\2(+\3\2\2\2)\'\3\2\2\2*,\t\2\2\2+*\3" +
                    "\2\2\2,-\3\2\2\2-+\3\2\2\2-.\3\2\2\2.\3\3\2\2\2/\60\7\30\2\2\60\5\3\2" +
                    "\2\2\61\62\5\4\3\2\62\66\5\2\2\2\63\65\7\24\2\2\64\63\3\2\2\2\658\3\2" +
                    "\2\2\66\64\3\2\2\2\66\67\3\2\2\2\67\7\3\2\2\28\66\3\2\2\29D\5\6\4\2:<" +
                    "\7\24\2\2;:\3\2\2\2<?\3\2\2\2=;\3\2\2\2=>\3\2\2\2>@\3\2\2\2?=\3\2\2\2" +
                    "@A\7\t\2\2AC\5\6\4\2B=\3\2\2\2CF\3\2\2\2DB\3\2\2\2DE\3\2\2\2E\t\3\2\2" +
                    "\2FD\3\2\2\2GH\7\n\2\2HI\5\b\5\2IJ\7\13\2\2JM\3\2\2\2KM\5\b\5\2LG\3\2" +
                    "\2\2LK\3\2\2\2M\13\3\2\2\2Nl\7\30\2\2OQ\7\24\2\2PO\3\2\2\2QR\3\2\2\2R" +
                    "P\3\2\2\2RS\3\2\2\2ST\3\2\2\2TV\7\f\2\2UW\7\24\2\2VU\3\2\2\2WX\3\2\2\2" +
                    "XV\3\2\2\2XY\3\2\2\2Yl\3\2\2\2Z\\\7\24\2\2[Z\3\2\2\2\\]\3\2\2\2][\3\2" +
                    "\2\2]^\3\2\2\2^_\3\2\2\2_a\7\r\2\2`b\7\24\2\2a`\3\2\2\2bc\3\2\2\2ca\3" +
                    "\2\2\2cd\3\2\2\2de\3\2\2\2eg\7\f\2\2fh\7\24\2\2gf\3\2\2\2hi\3\2\2\2ig" +
                    "\3\2\2\2ij\3\2\2\2jl\3\2\2\2kN\3\2\2\2kP\3\2\2\2k[\3\2\2\2l\r\3\2\2\2" +
                    "mo\7\24\2\2nm\3\2\2\2or\3\2\2\2pn\3\2\2\2pq\3\2\2\2qs\3\2\2\2rp\3\2\2" +
                    "\2st\t\3\2\2t\17\3\2\2\2uv\5\16\b\2vw\5\f\7\2wx\5\16\b\2x\u008a\3\2\2" +
                    "\2y{\7\24\2\2zy\3\2\2\2{~\3\2\2\2|z\3\2\2\2|}\3\2\2\2}\177\3\2\2\2~|\3" +
                    "\2\2\2\177\u0080\7\n\2\2\u0080\u0084\5\26\f\2\u0081\u0083\7\24\2\2\u0082" +
                    "\u0081\3\2\2\2\u0083\u0086\3\2\2\2\u0084\u0082\3\2\2\2\u0084\u0085\3\2" +
                    "\2\2\u0085\u0087\3\2\2\2\u0086\u0084\3\2\2\2\u0087\u0088\7\13\2\2\u0088" +
                    "\u008a\3\2\2\2\u0089u\3\2\2\2\u0089|\3\2\2\2\u008a\21\3\2\2\2\u008b\u008f" +
                    "\5\20\t\2\u008c\u008e\7\24\2\2\u008d\u008c\3\2\2\2\u008e\u0091\3\2\2\2" +
                    "\u008f\u008d\3\2\2\2\u008f\u0090\3\2\2\2\u0090\u0092\3\2\2\2\u0091\u008f" +
                    "\3\2\2\2\u0092\u0093\7\16\2\2\u0093\u0094\5\20\t\2\u0094\u0097\3\2\2\2" +
                    "\u0095\u0097\5\20\t\2\u0096\u008b\3\2\2\2\u0096\u0095\3\2\2\2\u0097\23" +
                    "\3\2\2\2\u0098\u009c\5\22\n\2\u0099\u009b\7\24\2\2\u009a\u0099\3\2\2\2" +
                    "\u009b\u009e\3\2\2\2\u009c\u009a\3\2\2\2\u009c\u009d\3\2\2\2\u009d\u009f" +
                    "\3\2\2\2\u009e\u009c\3\2\2\2\u009f\u00a0\7\17\2\2\u00a0\u00a1\5\22\n\2" +
                    "\u00a1\u00a4\3\2\2\2\u00a2\u00a4\5\22\n\2\u00a3\u0098\3\2\2\2\u00a3\u00a2" +
                    "\3\2\2\2\u00a4\25\3\2\2\2\u00a5\u00a6\5\24\13\2\u00a6\27\3\2\2\2\u00a7" +
                    "\u00ab\7\20\2\2\u00a8\u00aa\7\24\2\2\u00a9\u00a8\3\2\2\2\u00aa\u00ad\3" +
                    "\2\2\2\u00ab\u00a9\3\2\2\2\u00ab\u00ac\3\2\2\2\u00ac\u00ae\3\2\2\2\u00ad" +
                    "\u00ab\3\2\2\2\u00ae\u00af\5\26\f\2\u00af\31\3\2\2\2\u00b0\u00b1\7\32" +
                    "\2\2\u00b1\33\3\2\2\2\u00b2\u00c3\5\32\16\2\u00b3\u00b5\7\24\2\2\u00b4" +
                    "\u00b3\3\2\2\2\u00b5\u00b8\3\2\2\2\u00b6\u00b4\3\2\2\2\u00b6\u00b7\3\2" +
                    "\2\2\u00b7\u00b9\3\2\2\2\u00b8\u00b6\3\2\2\2\u00b9\u00bd\7\t\2\2\u00ba" +
                    "\u00bc\7\24\2\2\u00bb\u00ba\3\2\2\2\u00bc\u00bf\3\2\2\2\u00bd\u00bb\3" +
                    "\2\2\2\u00bd\u00be\3\2\2\2\u00be\u00c0\3\2\2\2\u00bf\u00bd\3\2\2\2\u00c0" +
                    "\u00c2\5\32\16\2\u00c1\u00b6\3\2\2\2\u00c2\u00c5\3\2\2\2\u00c3\u00c1\3" +
                    "\2\2\2\u00c3\u00c4\3\2\2\2\u00c4\35\3\2\2\2\u00c5\u00c3\3\2\2\2\u00c6" +
                    "\u00ca\7\21\2\2\u00c7\u00c9\7\24\2\2\u00c8\u00c7\3\2\2\2\u00c9\u00cc\3" +
                    "\2\2\2\u00ca\u00c8\3\2\2\2\u00ca\u00cb\3\2\2\2\u00cb\u00ce\3\2\2\2\u00cc" +
                    "\u00ca\3\2\2\2\u00cd\u00cf\5\34\17\2\u00ce\u00cd\3\2\2\2\u00ce\u00cf\3" +
                    "\2\2\2\u00cf\u00d3\3\2\2\2\u00d0\u00d2\7\24\2\2\u00d1\u00d0\3\2\2\2\u00d2" +
                    "\u00d5\3\2\2\2\u00d3\u00d1\3\2\2\2\u00d3\u00d4\3\2\2\2\u00d4\u00d6\3\2" +
                    "\2\2\u00d5\u00d3\3\2\2\2\u00d6\u00d7\7\22\2\2\u00d7\37\3\2\2\2\u00d8\u00dc" +
                    "\5\32\16\2\u00d9\u00db\7\24\2\2\u00da\u00d9\3\2\2\2\u00db\u00de\3\2\2" +
                    "\2\u00dc\u00da\3\2\2\2\u00dc\u00dd\3\2\2\2\u00dd\u00e0\3\2\2\2\u00de\u00dc" +
                    "\3\2\2\2\u00df\u00e1\5\36\20\2\u00e0\u00df\3\2\2\2\u00e0\u00e1\3\2\2\2" +
                    "\u00e1\u00e5\3\2\2\2\u00e2\u00e4\7\24\2\2\u00e3\u00e2\3\2\2\2\u00e4\u00e7" +
                    "\3\2\2\2\u00e5\u00e3\3\2\2\2\u00e5\u00e6\3\2\2\2\u00e6\u00e9\3\2\2\2\u00e7" +
                    "\u00e5\3\2\2\2\u00e8\u00ea\5\n\6\2\u00e9\u00e8\3\2\2\2\u00e9\u00ea\3\2" +
                    "\2\2\u00ea\u00ee\3\2\2\2\u00eb\u00ed\7\24\2\2\u00ec\u00eb\3\2\2\2\u00ed" +
                    "\u00f0\3\2\2\2\u00ee\u00ec\3\2\2\2\u00ee\u00ef\3\2\2\2\u00ef\u00f2\3\2" +
                    "\2\2\u00f0\u00ee\3\2\2\2\u00f1\u00f3\5\30\r\2\u00f2\u00f1\3\2\2\2\u00f2" +
                    "\u00f3\3\2\2\2\u00f3!\3\2\2\2\u00f4\u00f6\7\24\2\2\u00f5\u00f4\3\2\2\2" +
                    "\u00f6\u00f9\3\2\2\2\u00f7\u00f5\3\2\2\2\u00f7\u00f8\3\2\2\2\u00f8\u00fa" +
                    "\3\2\2\2\u00f9\u00f7\3\2\2\2\u00fa\u00fe\5 \21\2\u00fb\u00fd\7\24\2\2" +
                    "\u00fc\u00fb\3\2\2\2\u00fd\u0100\3\2\2\2\u00fe\u00fc\3\2\2\2\u00fe\u00ff" +
                    "\3\2\2\2\u00ff\u0104\3\2\2\2\u0100\u00fe\3\2\2\2\u0101\u0103\7\23\2\2" +
                    "\u0102\u0101\3\2\2\2\u0103\u0106\3\2\2\2\u0104\u0102\3\2\2\2\u0104\u0105" +
                    "\3\2\2\2\u0105#\3\2\2\2\u0106\u0104\3\2\2\2&\'-\66=DLRX]cikp|\u0084\u0089" +
                    "\u008f\u0096\u009c\u00a3\u00ab\u00b6\u00bd\u00c3\u00ca\u00ce\u00d3\u00dc" +
                    "\u00e0\u00e5\u00e9\u00ee\u00f2\u00f7\u00fe\u0104";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());

    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}
