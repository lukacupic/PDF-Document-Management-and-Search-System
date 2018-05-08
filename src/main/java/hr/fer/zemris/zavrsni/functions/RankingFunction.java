package hr.fer.zemris.zavrsni.functions;

import hr.fer.zemris.zavrsni.model.Result;

import java.util.List;

public interface RankingFunction {

	List<Result> process(String query);
}
