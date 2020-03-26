package com.jvtrodrigues.minhasfinancas.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Example;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.jvtrodrigues.minhasfinancas.exception.RegraNegocioException;
import com.jvtrodrigues.minhasfinancas.model.entity.Lancamento;
import com.jvtrodrigues.minhasfinancas.model.entity.Usuario;
import com.jvtrodrigues.minhasfinancas.model.enuns.StatusLancamento;
import com.jvtrodrigues.minhasfinancas.model.enuns.TipoLancamento;
import com.jvtrodrigues.minhasfinancas.model.repository.LancamentoRepository;
import com.jvtrodrigues.minhasfinancas.model.repository.LancamentoRepositoryTest;
import com.jvtrodrigues.minhasfinancas.service.impl.LancamentoServiceImpl;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LancamentoServiceTest {

	@SpyBean
	LancamentoServiceImpl service;
	@MockBean
	LancamentoRepository repository;

	@Test
	public void deveSalvarUmLancamento() {
		// cenario
		Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
		Mockito.doNothing().when(service).validar(lancamentoASalvar);

		Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
		Mockito.when(repository.save(lancamentoASalvar)).thenReturn(lancamentoSalvo);
		lancamentoSalvo.setId(1l);
		lancamentoSalvo.setStatus(StatusLancamento.PENDENTE);

		// execucao
		Lancamento lancamento = service.salvar(lancamentoASalvar);

		// verificacao
		Assertions.assertThat(lancamento.getId()).isEqualTo(lancamentoSalvo.getId());
		Assertions.assertThat(lancamento.getStatus()).isEqualTo(StatusLancamento.PENDENTE);
	}

	@Test
	public void naoDeveSalvarUmLancamentoQuandoHouveErroDeValidacao() {
		// cenario
		Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
		Mockito.doThrow(RegraNegocioException.class).when(service).validar(lancamentoASalvar);

		// exec e verificacao
		Assertions.catchThrowableOfType(() -> service.salvar(lancamentoASalvar), RegraNegocioException.class);
		Mockito.verify(repository, Mockito.never()).save(lancamentoASalvar);
	}

	@Test
	public void deveAtualizarUmLancamento() {
		// cenario
		Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
		lancamentoSalvo.setId(1l);
		lancamentoSalvo.setStatus(StatusLancamento.PENDENTE);

		Mockito.doNothing().when(service).validar(lancamentoSalvo);

		Mockito.when(repository.save(lancamentoSalvo)).thenReturn(lancamentoSalvo);
		// execucao
		service.atualizar(lancamentoSalvo);

		// verificacao
		Mockito.verify(repository, Mockito.times(1)).save(lancamentoSalvo);
	}

	@Test
	public void deveLancarErroAoTentarAtualizarUmLancamentoQueAindaNaoFoiSalvo() {
		// cenario
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();

		// exec e verificacao
		Assertions.catchThrowableOfType(() -> service.atualizar(lancamento), NullPointerException.class);
		Mockito.verify(repository, Mockito.never()).save(lancamento);
	}

	@Test
	public void deveDeletarUmLancamento() {
		// cenario
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1l);

		// execucao
		service.deletar(lancamento);

		// verificacao
		Mockito.verify(repository).delete(lancamento);
	}

	@Test
	public void deveLancarErroAoTentarDeletarUmLancamentoQueAindaNaoFoiSalvo() {
		// cenario
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();

		// execucao
		Assertions.catchThrowableOfType(() -> service.deletar(lancamento), NullPointerException.class);

		// verificacao
		Mockito.verify(repository, Mockito.never()).delete(lancamento);
	}

	@Test
	public void deveFiltrarLancamentos() {
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1l);

		List<Lancamento> lista = Arrays.asList(lancamento);
		Mockito.when(repository.findAll(Mockito.any(Example.class))).thenReturn(lista);

		// execucao
		List<Lancamento> resultado = service.buscar(lancamento);

		// verificacao
		Assertions.assertThat(resultado).isNotEmpty().hasSize(1).contains(lancamento);
	}
	
	@Test
	public void deveAtualizarOStatusDeUmLancamento() {
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1l);
		lancamento.setStatus(StatusLancamento.PENDENTE);
		
		StatusLancamento novoStatus = StatusLancamento.EFETIVADO;
		Mockito.doReturn(lancamento).when(service).atualizar(lancamento);
		
		//execucao
		service.atualizarStatus(lancamento, novoStatus);
		
		//verificacao
		Assertions.assertThat(lancamento.getStatus()).isEqualTo(novoStatus);
		Mockito.verify(service).atualizar(lancamento);
	}
	
	@Test
	public void deveObterUmLancamentoPorId() {
		//cenario
		Long id = 1l;
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(id);
		
		Mockito.when(repository.findById(id)).thenReturn(Optional.of(lancamento));
		
		//execucao
		Optional<Lancamento> resultado = service.obterPorId(id);
		
		//verificacao
		Assertions.assertThat(resultado.isPresent()).isTrue();
	}
	
	@Test
	public void deveRetornarVazioQuandoUmLancamentoNaoExiste() {
		//cenario
		Long id = 1l;
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(id);
		
		Mockito.when(repository.findById(id)).thenReturn(Optional.empty());
		
		//execucao
		Optional<Lancamento> resultado = service.obterPorId(id);
		
		//verificacao
		Assertions.assertThat(resultado.isPresent()).isFalse();
	}
	
	@Test
	public void deveLancarErrosAoValidarUmLancamento() {
		//cenario
		Lancamento lancamento = new Lancamento();
		//id inválido
		Throwable erro = Assertions.catchThrowable( () -> service.validar(lancamento));
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe uma Descrição válida.");
		
		lancamento.setDescricao("");
		
		erro = Assertions.catchThrowable( () -> service.validar(lancamento));
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe uma Descrição válida.");
		
		lancamento.setDescricao("Salario");
		
		erro = Assertions.catchThrowable( () -> service.validar(lancamento));
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Mês válido.");
		
		lancamento.setMes(3);
		
		erro = Assertions.catchThrowable( () -> service.validar(lancamento));
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Ano válido.");
		
		lancamento.setAno(0);
		
		erro = Assertions.catchThrowable( () -> service.validar(lancamento));
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Ano válido.");
		
		lancamento.setAno(13);
		
		erro = Assertions.catchThrowable( () -> service.validar(lancamento));
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Ano válido.");
		
		lancamento.setAno(212);
		
		erro = Assertions.catchThrowable( () -> service.validar(lancamento));
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Ano válido.");
		
		lancamento.setAno(2020);
		
		erro = Assertions.catchThrowable( () -> service.validar(lancamento));
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Usuário.");
		
		lancamento.setUsuario(new Usuario());
		
		erro = Assertions.catchThrowable( () -> service.validar(lancamento));
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Usuário.");
		
		lancamento.getUsuario().setId(1l);	
		
		erro = Assertions.catchThrowable( () -> service.validar(lancamento));
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Valor válido.");
		
		lancamento.setValor(BigDecimal.ZERO);
		
		erro = Assertions.catchThrowable( () -> service.validar(lancamento));
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Valor válido.");
		
		lancamento.setValor(BigDecimal.valueOf(230));
		
		erro = Assertions.catchThrowable( () -> service.validar(lancamento));
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um tipo de Lançamento.");
		
		lancamento.setTipo(TipoLancamento.RECEITA);
	}
	
	/*@Test implementar...
	public void deveObterSaldoPorUsuario() {
		
	}*/
}
